package com.amos.crpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer {
    @Override
    public void doStart(int port) {
        // 创建Vert.x实例
        Vertx vertx = Vertx.vertx();

        // 创建Http服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        // 监听端口并处理请求
//        server.requestHandler(request -> {
//            // 处理Http请求
//            System.out.println("Receice request: " + request);
//
//            // 发动Http响应
//            HttpServerResponse response = request.response();
//            response.putHeader("content-type", "text/plain");
//            // Write to the response and end it
//            response.end("Hello World!");
//        });
        server.requestHandler(new HttpServerHandler());

        // 启动Http服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.out.println("Failed to start Server: " + result.cause());
            }
        });
    }
}
