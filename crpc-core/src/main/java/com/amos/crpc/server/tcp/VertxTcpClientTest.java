package com.amos.crpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

public class VertxTcpClientTest {
    public void start(int port) {
        // 创建Vert.x实例
        Vertx vertx = Vertx.vertx();

        // 创建Tcp客户端
        vertx.createNetClient().connect(port, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("Connected to TCP server");
                NetSocket socket = result.result();
                // 发送数据
                for (int i = 0; i < 1000; i++) {
                    // 控制发送间隔，避免发送过快
//                    try {
//                        Thread.sleep(1); // 每条消息间隔 50 毫秒
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                    socket.write("Hello, server!Hello, server!Hello, server!Hello, server!");

                    Buffer buffer = Buffer.buffer();
                    String str = "Hello, server!Hello, server!Hello, server!Hello, server!";
                    buffer.appendInt(0);
                    buffer.appendInt(str.getBytes().length);
                    buffer.appendBytes(str.getBytes());
                    socket.write(buffer);
                }
                // 接收数据
                socket.handler(buffer -> {
                    System.out.println("Received data: " + buffer.toString());
                });
            } else {
                System.out.println("Failed to connect to server: " + result.cause());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpClientTest().start(8888);
    }
}
