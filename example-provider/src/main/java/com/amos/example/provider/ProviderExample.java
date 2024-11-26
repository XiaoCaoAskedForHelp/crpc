package com.amos.example.provider;

import com.amos.crpc.RpcApplication;
import com.amos.crpc.config.RegistryConfig;
import com.amos.crpc.config.RpcConfig;
import com.amos.crpc.model.ServiceMetaInfo;
import com.amos.crpc.registry.LocalRegistry;
import com.amos.crpc.registry.Registry;
import com.amos.crpc.registry.RegistryFactory;
import com.amos.crpc.server.HttpServer;
import com.amos.crpc.server.http.VertxHttpServer;
import com.amos.crpc.server.tcp.VertxTcpServer;
import com.amos.example.common.service.UserService;

/**
 * 服务提供者示例
 */
public class ProviderExample {
    public static void main(String[] args) {
        // RPC框架初识化
        RpcApplication.init();

        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 启动web服务
//        HttpServer httpServer = new VertxHttpServer();
//        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());

        // 启动TCP服务
        HttpServer httpServer = new VertxTcpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
