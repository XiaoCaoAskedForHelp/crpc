package com.amos.crpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.amos.crpc.RpcApplication;
import com.amos.crpc.model.RpcRequest;
import com.amos.crpc.model.RpcResponse;
import com.amos.crpc.model.ServiceMetaInfo;
import com.amos.crpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Vert.x TCP 请求客户端
 */
public class VertxTcpClient {
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        // 发动tcp请求
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        // 异步处理,等待响应
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(), result -> {
            if (result.succeeded()) {
                System.out.println("request Connected to TCP server");
                NetSocket socket = result.result();
                // 发送消息
                ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                ProtocolMessage.Header header = new ProtocolMessage.Header();
                header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                // 生成全局唯一请求ID
                header.setRequestId(IdUtil.getSnowflakeNextId());
                protocolMessage.setHeader(header);
                protocolMessage.setBody(rpcRequest);
                // 编码请求
                try {
                    Buffer requestBuffer = ProcotolMessageEncoder.encode(protocolMessage);
                    socket.write(requestBuffer);
                } catch (IOException e) {
                    throw new RuntimeException("协议信息编码失败");
                }
//                socket.handler(buffer -> {
//                    try {
//                        ProtocolMessage<RpcResponse> responseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
//                        RpcResponse rpcResponse = responseProtocolMessage.getBody();
//                        responseFuture.complete(rpcResponse);
//                    } catch (IOException e) {
//                        throw new RuntimeException("协议信息解码失败");
//                    }
//                });
                // 接收响应
                TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(
                        buffer -> {
                            try {
                                ProtocolMessage<RpcResponse> responseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                RpcResponse rpcResponse = responseProtocolMessage.getBody();
                                responseFuture.complete(rpcResponse);
                            } catch (IOException e) {
                                throw new RuntimeException("协议信息解码失败");
                            }
                        }
                );
                socket.handler(bufferHandlerWrapper);
            } else {
                System.out.println("Failed to connect to TCP server: " + result.cause());
            }
        });
        RpcResponse rpcResponse = responseFuture.get();
        // 关闭连接
        netClient.close();
        return rpcResponse;
    }
}
