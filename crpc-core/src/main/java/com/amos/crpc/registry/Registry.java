package com.amos.crpc.registry;

import com.amos.crpc.config.RegistryConfig;
import com.amos.crpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 注册中心
 */
public interface Registry {
    /**
     * 初始化
     *
     * @param registryConfig
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务（服务端）
     *
     * @param serviceMetaInfo
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 服务发现（客户端）
     *
     * @param serviceKey
     * @return
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     * 注销服务
     *
     * @param serviceMetaInfo
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo);

    /**
     * 销毁
     */
    void destroy();

    /**
     * 心跳检测
     */
    void heartbeat();

    /**
     * 监听（消费端）
     * @param serviceNodeKey
     */
    void watch(String serviceNodeKey);
}