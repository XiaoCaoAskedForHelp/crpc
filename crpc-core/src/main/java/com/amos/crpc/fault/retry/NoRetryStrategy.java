package com.amos.crpc.fault.retry;

import com.amos.crpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 不重试-重试策略，直接执行一次任务
 */
public class NoRetryStrategy implements RetryStrategy {

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
