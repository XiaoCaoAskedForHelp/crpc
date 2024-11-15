package com.amos.crpc.bootstrap;

import com.amos.crpc.RpcApplication;

/**
 * 消费者启动类(初始化)
 */
public class ConsumerBootstrap {
    /**
     * 初始化
     */
    public static void init() {
        // 初始化RPC框架
        RpcApplication.init();
    }
}
