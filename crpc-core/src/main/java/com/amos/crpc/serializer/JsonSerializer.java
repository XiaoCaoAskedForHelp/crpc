package com.amos.crpc.serializer;

import com.amos.crpc.model.RpcRequest;
import com.amos.crpc.model.RpcResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * JSON序列化器
 */
public class JsonSerializer implements Serializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    /**
     * 假设有一个类 RpcRequest，它的一个字段是 Object[] args，其中 args 数组存储着不同类型的参数。在序列化时，args 数组会被转化为字节流，
     * 这时候泛型信息就会丢失。当我们反序列化字节流时，args 中的元素会被错误地反序列化成 LinkedHashMap 类型，因为 Object 被擦除，导致反序列化时无法推断出原始类型。
     *
     * @param bytes
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        T obj = OBJECT_MAPPER.readValue(bytes, type);
        if (obj instanceof RpcRequest) {
            return handleRpcRequest((RpcRequest)obj, type);
        }
        if (obj instanceof RpcResponse){
            return handleRpcResponse((RpcResponse)obj, type);
        }
        return obj;
    }

    /**
     * 由于Object的原始对象会被擦除，导致反序列化时会被错误地反序列化成 LinkedHashMap 类型，无法转换为原始对象，因此需要重新处理
     * @param rpcRequest
     * @param type
     * @return
     * @param <T>
     * @throws IOException
     */
    private <T> T handleRpcRequest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();

        for (int i = 0; i < parameterTypes.length; i++) {
            if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                // 如果参数类型不匹配，重新序列化
                // 简单处理，直接转换类型
                args[i] = OBJECT_MAPPER.convertValue(args[i], parameterTypes[i]);
                // 复杂类型处理
//                byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
//                args[i] = OBJECT_MAPPER.readValue(bytes, parameterTypes[i]);
            }
        }
        return type.cast(rpcRequest);
    }

    private <T> T handleRpcResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        // 处理响应数据
        Object data = rpcResponse.getData();
        if (data != null && !rpcResponse.getDataType().isAssignableFrom(data.getClass())) {
            rpcResponse.setData(OBJECT_MAPPER.convertValue(data, rpcResponse.getDataType()));
        }
        return type.cast(rpcResponse);

//        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
//        rpcResponse.setData(OBJECT_MAPPER.readValue(bytes, rpcResponse.getDataType()));
//        return type.cast(rpcResponse);
    }

}
