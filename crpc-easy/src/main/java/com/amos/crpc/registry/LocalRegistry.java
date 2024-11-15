package com.amos.crpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地注册中心
 * 使用现成安全的ConcurrentHashMap存储服务注册信息
 * key为服务名称，value为获取到对应的实现类
 * 然后通过反射进行方法调用
 */
public class LocalRegistry {
    /**
     * 注册信息存储
     */
    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     */
    public static void register(String serviceName, Class<?> implClass) {
        map.put(serviceName, implClass);
    }

    /**
     * 获取服务
     */
    public static Class<?> get(String serviceName) {
        return map.get(serviceName);
    }

    /**
     * 删除服务
     */
    public static void remove(String serviceName) {
        map.remove(serviceName);
    }
}
