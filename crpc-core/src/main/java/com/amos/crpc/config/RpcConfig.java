package com.amos.crpc.config;


import com.amos.crpc.fault.retry.RetryStrategyKeys;
import com.amos.crpc.fault.tolerant.TolerantStrategyKeys;
import com.amos.crpc.loadbalancer.LoadBalancerKeys;
import com.amos.crpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC框架配置
 */
@Data
public class RpcConfig {
    /**
     * 名称
     */
    private String name = "crpc";

    /**
     * 版本号
     */
    private String version = "1.0.0";

    /**
     * 服务主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务端口
     */
    private int serverPort = 8080;

    /**
     * 模拟调用
     */
    private boolean mock = false;

    /**
     * 序列化方式
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 负载均衡策略
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;


    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();

}
