package com.amos.crpc.registry;

import com.amos.crpc.spi.SpiLoader;

/**
 * 工厂模式，支持根据key从SPI获取对应的注册中心对象实例
 */
public class RegistryFactory {
    static {
        SpiLoader.load(Registry.class);
    }

    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 根据key获取注册中心对象
     *
     * @param key
     * @return
     */
    public static Registry getRegistry(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }
}
