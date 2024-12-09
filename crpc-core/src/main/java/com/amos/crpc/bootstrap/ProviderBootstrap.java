package com.amos.crpc.bootstrap;

import com.amos.crpc.RpcApplication;
import com.amos.crpc.config.RegistryConfig;
import com.amos.crpc.config.RpcConfig;
import com.amos.crpc.model.ServiceMetaInfo;
import com.amos.crpc.model.ServiceRegisterInfo;
import com.amos.crpc.registry.LocalRegistry;
import com.amos.crpc.registry.Registry;
import com.amos.crpc.registry.RegistryFactory;
import com.amos.crpc.server.HttpServer;
import com.amos.crpc.server.tcp.VertxTcpServer;

import java.util.List;

/**
 * 服务提供者初始化
 */
public class ProviderBootstrap {
    /**
     * 初始化
     */
    public static void init(List<ServiceRegisterInfo> serviceRegisterInfoList) {
        // RPC框架初识化
        RpcApplication.init();

        // 注册服务到注册中心
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        for (ServiceRegisterInfo serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            // 本地注册
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + "服务注册失败", e);
            }
        }

        // 启动web服务
//        HttpServer httpServer = new VertxHttpServer();
//        httpServer.doStart(rpcConfig.getServerPort());

        // 启动TCP服务
        HttpServer httpServer = new VertxTcpServer();
        httpServer.doStart(rpcConfig.getServerPort());
    }
}
