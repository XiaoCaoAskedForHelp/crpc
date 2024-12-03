package com.amos.crpc.fault.retry;

/**
 * 重试策略键名常量
 */
public interface RetryStrategyKeys {
    /**
     * 不重试
     */
    String NO = "no";

    /**
     * 固定间隔
     */
    String FIXED_INTERVAL = "fixedInterval";
}
