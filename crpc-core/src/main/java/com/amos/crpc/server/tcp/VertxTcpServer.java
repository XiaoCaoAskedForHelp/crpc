package com.amos.crpc.server.tcp;

import com.amos.crpc.server.HttpServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxTcpServer implements HttpServer {
    public byte[] handleRequest(byte[] requestData) {
        // 在这里进行自定义的字节数组处理逻辑，比如解析请求数据包
        return "Hello, World!".getBytes();
    }

    @Override
    public void doStart(int port) {
        // 创建Vert.x实例
        Vertx vertx = Vertx.vertx();

        // 创建Tcp服务器
        NetServer server = vertx.createNetServer();

        // 监听端口并处理请求
//        server.connectHandler(netSocket -> {
//            // 处理连接
////            netSocket.handler(buffer -> {
////                String testMessage = "Hello, server!Hello, server!Hello, server!Hello, server!";
////                int messageLength = testMessage.getBytes().length;
////                if (buffer.getBytes().length < messageLength) {
////                    System.out.println("半包, length: " + buffer.getBytes().length);
////                    return;
////                }
////                if (buffer.getBytes().length > messageLength) {
////                    System.out.println("粘包, length: " + buffer.getBytes().length);
////                    return;
////                }
////                String str = new String(buffer.getBytes(0, messageLength));
////                if (testMessage.equals(str)) {
////                    System.out.println("Correct data length: " + messageLength);
////                    System.out.println("Received correct data: " + str);
////                } else {
////                    System.out.println("Received incorrect data: " + str);
////                }
////
////                // 处理接受到的字节数组
////                byte[] requestData = buffer.getBytes();
////                // 在这里进行自定义的字节数组处理逻辑，比如解析请求数据包
////                byte[] responseData = handleRequest(requestData);
////                // 发送响应数据
////                netSocket.write(Buffer.buffer(responseData));
////            });
//
////            String testMessage = "Hello, server!Hello, server!Hello, server!Hello, server!";
////            int messageLength = testMessage.getBytes().length;
////            // 构造parser
////            RecordParser parser = RecordParser.newFixed(messageLength);
////            parser.setOutput(new Handler<Buffer>() {
////                @Override
////                public void handle(Buffer buffer) {
////                    String str = new String(buffer.getBytes());
////                    System.out.println(str);
////                    if (testMessage.equals(str)) {
////                        System.out.println("good");
////                    }
////                }
////            });
////            netSocket.handler(parser);
//
//            // 构造parser
//            RecordParser parser = RecordParser.newFixed(8);
//            parser.setOutput(new Handler<Buffer>() {
//                // 初始化
//                int size = -1;
//                // 一次完整的读取（头 + 体）
//                Buffer resultbuffer = Buffer.buffer();
//
//                @Override
//                public void handle(Buffer buffer) {
//                    if (-1 == size) {
//                        // 读取消息体长度
//                        size = buffer.getInt(4);
//                        parser.fixedSizeMode(size);
//                        // 写入头信息到结果
//                        resultbuffer.appendBuffer(buffer);
//                    }else{
//                        // 读取消息体
//                        resultbuffer.appendBuffer(buffer);
//                        // 读取完毕
//                        System.out.println(resultbuffer.toString());
//                        // 重置
//                        size = -1;
//                        parser.fixedSizeMode(8);
//                        resultbuffer = Buffer.buffer();
//                    }
//                }
//            });
//            netSocket.handler(parser);
//        });
        server.connectHandler(new TcpServerHandler());

        // 启动Tcp服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                log.info("TCP Server is now listening on port " + port);
            } else {
                log.error("Failed to start TCP Server: " + result.cause());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8888);
    }
}
