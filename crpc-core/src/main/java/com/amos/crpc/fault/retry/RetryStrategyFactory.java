package com.amos.crpc.fault.retry;

import com.amos.crpc.spi.SpiLoader;

public class RetryStrategyFactory {
    static {
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 默认重试器
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static RetryStrategy getRetryStrategy(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }
}
