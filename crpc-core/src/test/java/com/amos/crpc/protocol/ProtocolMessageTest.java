package com.amos.crpc.protocol;

import cn.hutool.core.util.IdUtil;
import com.amos.crpc.model.RpcRequest;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;

import java.io.IOException;

public class ProtocolMessageTest {

    @Test
    public void testEncodeAndDecode() throws IOException {
        // 构造消息
        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setSerializer((byte) ProtocolMessageSerializerEnum.JDK.getKey());
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
        header.setRequestId(IdUtil.getSnowflakeNextId());
        header.setBodyLength(0);  // 在encode会根据body的长度重新赋值
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("com.amos.crpc.service.UserService");
        rpcRequest.setMethodName("findById");
        rpcRequest.setParameterTypes(new Class[]{String.class});
        rpcRequest.setArgs(new Object[]{"1"});
        protocolMessage.setHeader(header);
        protocolMessage.setBody(rpcRequest);

        // 编码
        Buffer buffer = ProcotolMessageEncoder.encode(protocolMessage);
        // 解码
        ProtocolMessage<?> decodeMessage = ProtocolMessageDecoder.decode(buffer);
        System.out.println(decodeMessage);
    }
}
