package com.amos.crpc;

import com.amos.crpc.config.RegistryConfig;
import com.amos.crpc.config.RpcConfig;
import com.amos.crpc.constant.RpcConstant;
import com.amos.crpc.registry.Registry;
import com.amos.crpc.registry.RegistryFactory;
import com.amos.crpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC框架应用
 * 相当于holder，存放了项目全局用到的变量，双检锁单例模式实现
 */
@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初识化，支持传入自定义配置
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("RPC框架初始化完成，当前配置：{}", rpcConfig.toString());

        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("注册中心初始化完成，当前配置：{}", registryConfig.toString());

        // 创建并注册ShutdownHook， JVM关闭时自动注销服务
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM关闭，注销服务");
            registry.destroy();
        }));
    }

    public static void init() {
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            log.error("加载配置文件失败", e);
            // 使用默认配置
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置
     *
     * @return
     */
    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }

}
