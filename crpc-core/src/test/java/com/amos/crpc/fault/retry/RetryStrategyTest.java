package com.amos.crpc.fault.retry;

import com.amos.crpc.model.RpcResponse;
import org.junit.Test;

/**
 * 重试策略测试
 */
public class RetryStrategyTest {
    RetryStrategy retryStrategy = new FixedIntervalRetryStrategy();

    @Test
    public void testDoRetry() {
        try {
            RpcResponse rpcResponse = retryStrategy.doRetry(() -> {
                System.out.println("执行任务");
                throw new RuntimeException("模拟重试失败");
            });
        } catch (Exception e) {
            System.out.println("重试多次失败");
            e.printStackTrace();
        }
    }
}
