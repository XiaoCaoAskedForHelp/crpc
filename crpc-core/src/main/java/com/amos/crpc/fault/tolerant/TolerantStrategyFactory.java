package com.amos.crpc.fault.tolerant;

import com.amos.crpc.loadbalancer.LoadBalancer;
import com.amos.crpc.loadbalancer.RandomLoadBalancer;
import com.amos.crpc.spi.SpiLoader;

/**
 * 容错策略工厂
 */
public class TolerantStrategyFactory {
    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    /**
     * 默认
     */
    private static final TolerantStrategy DEFAULT_TOLERANT_STRATEGY = new FailFastTolerantStrategy();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static TolerantStrategy getTolerantStrategy(String key) {
        return SpiLoader.getInstance(TolerantStrategy.class, key);
    }
}
