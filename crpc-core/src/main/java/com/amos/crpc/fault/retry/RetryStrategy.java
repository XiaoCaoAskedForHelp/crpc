package com.amos.crpc.fault.retry;

import com.amos.crpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 重试策略
 */
public interface RetryStrategy {
    /**
     * 重试
     *
     * @param callable
     * @return
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
