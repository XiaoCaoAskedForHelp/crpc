package com.amos.crpc.protocol;

import com.amos.crpc.model.RpcRequest;
import com.amos.crpc.model.RpcResponse;
import com.amos.crpc.serializer.Serializer;
import com.amos.crpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * 协议消息解码器
 */
public class ProtocolMessageDecoder {
    /**
     * 解码
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        // 分别从指定位置读出 Buffer
        byte magic = buffer.getByte(0);
        // 魔数校验
        if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new RuntimeException("魔数不正确");
        }
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));
        // 解决粘包问题，读取完整的消息体
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());
        // 解析消息体
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化器不存在");
        }
        Serializer serializer = SerializerFactory.getSerializer(serializerEnum.getValue());
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if (messageTypeEnum == null) {
            throw new RuntimeException("消息类型不存在");
        }
        switch (messageTypeEnum) {
            case REQUEST:
                RpcRequest rpcRequest = serializer.deserialize(bodyBytes, RpcRequest.class);
                return new ProtocolMessage<>(header, rpcRequest);
            case RESPONSE:
                RpcResponse rpcResponse = serializer.deserialize(bodyBytes, RpcResponse.class);
                return new ProtocolMessage<>(header, rpcResponse);
            case HEART_BEAT:
            default:
                throw new RuntimeException("消息类型不存在");
        }

    }
}
