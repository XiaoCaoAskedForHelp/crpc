package com.amos.crpc.serializer;

import com.amos.crpc.spi.SpiLoader;


/**
 * 序列化工厂
 * 序列化对象可以复用，不需要每次调用时都创建一个新的序列化对象
 * 通过工厂模式和单例模式创建
 */
public class SerializerFactory {
    static {
        SpiLoader.load(Serializer.class);
    }

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JDKSerializer();

    /**
     * 获取序列化器
     *
     * @param key
     * @return
     */
    public static Serializer getSerializer(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }
}
