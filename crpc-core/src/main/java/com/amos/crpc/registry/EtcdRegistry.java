package com.amos.crpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.amos.crpc.config.RegistryConfig;
import com.amos.crpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry {
    private Client client;

    private KV kvClient;

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * 本地注册节点缓存，用于续约
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 消费端注册中心服务缓存
     * （只支持单个服务缓存，已废弃，请使用下方的）
     */
    @Deprecated
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * 消费端注册中心服务缓存
     * （支持多个服务缓存）
     */
    private final RegistryServiceMultiCache registryServiceMultiCache = new RegistryServiceMultiCache();


    /**
     * 消费端正在监听的服务缓存
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    @Override
    public void init(RegistryConfig registryConfig) {
        // create client using endpoints
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        heartbeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建Lease客户端
        Lease leaseClient = client.getLeaseClient();
        // 创建一个30秒的租约
        long leaseId = leaseClient.grant(30).get().getID();

        // 设置要存储的键值对
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registryKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // 将键值对和租约绑定
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        // 添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registryKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先从缓存获取服务
        // （只支持单个服务缓存，已废弃，请使用下方的）
//        List<ServiceMetaInfo> cachedserviceMetaInfoList = registryServiceCache.readCache();
        // （支持多个服务缓存）
        List<ServiceMetaInfo> cachedserviceMetaInfoList = registryServiceMultiCache.readCache(serviceKey);
        if (CollUtil.isNotEmpty(cachedserviceMetaInfoList)) {
            System.out.println("从缓存获取服务信息");
            return cachedserviceMetaInfoList;
        }

        System.out.println("从注册中心获取服务信息");
        // 前缀查询
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            GetResponse response = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption).get();
            List<KeyValue> keyValues = response.getKvs();
            // 解析服务系统
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                    .map(keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        // 监听key的变化
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }).collect(Collectors.toList());

            // 写入缓存
            // （只支持单个服务缓存，已废弃，请使用下方的）
//            registryServiceCache.writeCache(serviceMetaInfoList);
            registryServiceMultiCache.writeCache(serviceKey, serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务信息失败", e);
        }

    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registryKey, StandardCharsets.UTF_8)).join();
        // 也要从本地缓存移除
        localRegisterNodeKeySet.remove(registryKey);
    }

    @Override
    public void destroy() {
        System.out.println("当前节点下线");

        // 下线节点
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败", e);
            }
        }

        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void heartbeat() {
        // 10秒续签一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点所有的key
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
                        // 该节点已被删除(需要重启节点才能重新注册)
                        if (CollUtil.isEmpty(keyValues)) {
                            localRegisterNodeKeySet.remove(key);
                            continue;
                        }
                        // 续约，节点未过期，重新注册
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续约失败", e);
                    }
                }
            }
        });
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 即使key在注册中心被删除在重新注册，之前的监听依旧生效，所以我们只监听首次注册的key
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            System.out.println("监听服务节点：" + serviceNodeKey);
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event : response.getEvents()) {
                    System.out.println("监听到服务节点变化：" + event.getEventType());
                    switch (event.getEventType()) {
                        case PUT:
                            break;
                        case DELETE:
                            // 清理注册服务缓存
                            System.out.println("服务下线，清理缓存");
                            // （只支持单个服务缓存，已废弃，请使用下方的）
//                            registryServiceCache.clearCache();
                            // fixme 这里需要改为serviceKey，而不是serviceNodeKey
                            String serviceKey = serviceNodeKey.split("/")[2];
                            System.out.println("serviceKey: " + serviceKey);
                            registryServiceMultiCache.clearCache(serviceKey);
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }
}
