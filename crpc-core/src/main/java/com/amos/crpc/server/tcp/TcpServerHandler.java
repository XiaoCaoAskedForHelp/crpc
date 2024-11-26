package com.amos.crpc.server.tcp;

import com.amos.crpc.model.RpcRequest;
import com.amos.crpc.model.RpcResponse;
import com.amos.crpc.protocol.ProcotolMessageEncoder;
import com.amos.crpc.protocol.ProtocolMessage;
import com.amos.crpc.protocol.ProtocolMessageDecoder;
import com.amos.crpc.protocol.ProtocolMessageTypeEnum;
import com.amos.crpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.lang.reflect.Method;

/**
 * TCP请求处理
 * 1.接受请求，解码
 * 2.处理请求
 * 3.返回响应，编码
 */
public class TcpServerHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {
        System.out.println("接收到请求");
        netSocket.handler(buffer -> {
            // 接受请求，解码
            ProtocolMessage<RpcRequest> protocolMessage = null;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
                System.out.println("Received request: " + protocolMessage);
            } catch (Exception e) {
                throw new RuntimeException("协议信息解码失败");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            // 处理请求
            RpcResponse rpcResponse = new RpcResponse();
            try {
                // 获取要调用的服务实现类，通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage("Server error: " + e.getMessage());
                rpcResponse.setException(e);
            }

            // 返回响应，编码
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer responseBuffer = ProcotolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(responseBuffer);
            } catch (Exception e) {
                throw new RuntimeException("协议信息编码失败");
            }
        });
    }
}
