package com.amos.example.provider;

import com.amos.crpc.registry.LocalRegistry;
import com.amos.crpc.server.HttpServer;
import com.amos.crpc.server.http.VertxHttpServer;
import com.amos.example.common.service.UserService;

/**
 * 简易服务提供者示例
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        // 提供服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
