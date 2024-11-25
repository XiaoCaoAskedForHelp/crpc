package com.amos.crpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import com.amos.crpc.config.RegistryConfig;
import com.amos.crpc.model.ServiceMetaInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ZooKeeper 注册中心
 */
public class ZooKeeperRegistry implements Registry {
    private CuratorFramework client;

    private ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;

    /**
     * 根节点
     */
    private static final String ZK_ROOT_PATH = "/rpc/zk";

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
        client = CuratorFrameworkFactory
                .builder()
                .connectString(registryConfig.getAddress())
                .retryPolicy(new ExponentialBackoffRetry(Math.toIntExact(registryConfig.getTimeout()), 3))
                .build();

        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaInfo.class)
                .client(client)
                .basePath(ZK_ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
                .build();

        try {
            // 启动client
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException("ZooKeeper注册中心初始化失败", e);
        }
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 注册到zk中
        serviceDiscovery.registerService(buildServiceInstance(serviceMetaInfo));
        String registryKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        // 添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registryKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        try {
            serviceDiscovery.unregisterService(buildServiceInstance(serviceMetaInfo));
        } catch (Exception e) {
            throw new RuntimeException("服务下线失败", e);
        }
        String registryKey = ZK_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        // 也要从本地缓存移除
        localRegisterNodeKeySet.remove(registryKey);
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
        try {
            // 查询服务信息
            Collection<ServiceInstance<ServiceMetaInfo>> serviceInstances = serviceDiscovery.queryForInstances(serviceKey);
            // 解析服务系统
            List<ServiceMetaInfo> serviceMetaInfoList = serviceInstances.stream()
                    .map(ServiceInstance::getPayload)
                    .collect(Collectors.toList());

            for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
                // 监听key的变化
                watch(serviceMetaInfo.getServiceNodeKey());
            }
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
    public void destroy() {
        System.out.println("当前节点下线");

        // 下线节点(这一步可以不做，因为都是临时节点，服务下线，自然就被删掉了)
        for (String key : localRegisterNodeKeySet) {
            try {
                client.delete().guaranteed().forPath(key);
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败", e);
            }
        }
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void heartbeat() {
        // 不需要心跳机制，建立了临时节点，如果服务器故障，则临时节点直接丢失
    }

    /**
     * 监听（消费端）
     *
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        String watchKey = ZK_ROOT_PATH + "/" + serviceNodeKey;
        // 即使key在注册中心被删除在重新注册，之前的监听依旧生效，所以我们只监听首次注册的key
        boolean newWatch = watchingKeySet.add(watchKey);
        if (newWatch) {
            System.out.println("监听服务节点：" + serviceNodeKey);
            CuratorCache curatorCache = CuratorCache.build(client, watchKey);
            curatorCache.start();
            curatorCache.listenable().addListener(
                    CuratorCacheListener.builder()
                            .forDeletes(node -> {
                                System.out.println("服务节点下线：" + serviceNodeKey);
                                // 从缓存中移除
                                String serviceKey = serviceNodeKey.split("/")[0];
                                registryServiceMultiCache.clearCache(serviceKey);
                            })
                            .forChanges((oldNode, node) -> {
                                System.out.println("服务节点变更：" + serviceNodeKey);
                                // 从缓存中移除
                                String serviceKey = serviceNodeKey.split("/")[0];
                                registryServiceMultiCache.clearCache(serviceKey);
                            })
                            .build()
            );
        }
    }

    private ServiceInstance<ServiceMetaInfo> buildServiceInstance(ServiceMetaInfo serviceMetaInfo) {
        String serviceAddress = serviceMetaInfo.getServiceHost() + ":" + serviceMetaInfo.getServicePort();
        try {
            return ServiceInstance.<ServiceMetaInfo>builder()
                    .id(serviceAddress)   // fixme 是不是应该是serviceMetaInfo.getServiceNodeKey()
                    .name(serviceMetaInfo.getServiceKey())
                    .address(serviceAddress)
                    .port(serviceMetaInfo.getServicePort())
                    .payload(serviceMetaInfo)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("构建ServiceInstance失败", e);
        }
    }
}
