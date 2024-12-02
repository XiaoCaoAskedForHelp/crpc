package com.amos.crpc.loadbalancer;

import com.amos.crpc.spi.SpiLoader;

/**
 * 负载均衡器工厂
 */
public class LoadBalancerFactory {
    static {
        SpiLoader.load(LoadBalancer.class);
    }

    /**
     * 默认负载均衡器
     */
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RandomLoadBalancer();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static LoadBalancer getLoadBalancer(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
