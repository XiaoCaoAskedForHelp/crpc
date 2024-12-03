package com.amos.crpc.fault.tolerant;

/**
 * 容错策略键名常量
 */
public interface TolerantStrategyKeys {

    /**
     * 快速失败
     */
    String FAIL_FAST = "failFast";

    /**
     * 降级到其他服务
     */
    String FAIL_BACK = "failBack";

    /**
     * 转移到其他服务节点
     */
    String FAIL_OVER = "failOver";

    /**
     * 静默处理
     */
    String Fail_Safe = "failSafe";
}
