package com.amos.crpc.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列化工厂
 * 序列化对象可以复用，不需要每次调用时都创建一个新的序列化对象
 * 通过工厂模式和单例模式创建
 */
public class SerializerFactory_old {
    /**
     * 根据序列化器Key获取序列化器,序列化映射（单例模式）
     */
    private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<String, Serializer>() {
        {
            put(SerializerKeys.JDK, new JDKSerializer());
            put(SerializerKeys.JSON, new JsonSerializer());
            put(SerializerKeys.KRYO, new KryoSerializer());
            put(SerializerKeys.HESSIAN, new HessianSerializer());
        }
    };

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = KEY_SERIALIZER_MAP.get(SerializerKeys.JDK);

    /**
     * 获取序列化器
     * @param key
     * @return
     */
    public static Serializer getSerializer(String key) {
        return KEY_SERIALIZER_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
    }
}
