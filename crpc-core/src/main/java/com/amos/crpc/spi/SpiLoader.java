package com.amos.crpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.amos.crpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI加载器,工具类，提供读取配置并加载实现类的功能
 * 1. 用Map存储已加载的配置信息 键名=>实现类
 * 2. 扫描指定路径，读取每个配置文件，加载实现类
 * 3. 根据用户传入的接口和键名，从MAP中找到对用的实现类，通过反射创建实现类对象，可以通过缓存提高性能
 */
@Slf4j
public class SpiLoader {
    /**
     * 存储已加载的类:接口名=>(key=>实现类)
     */
    private static final Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存
     */
    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统SPI配置文件路径
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 用户自定义SPI配置文件路径
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = {RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     */
    public static void loadAll() {
        for (Class<?> clazz : LOAD_CLASS_LIST) {
            load(clazz);
        }
    }

    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为 {} 的SPI配置", loadClass.getName());
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for (String scanDir : SCAN_DIRS) {
            // 使用ResourceUtil.getResources获取资源文件，而不是文件路径获取，因为如果框架作为依赖被打包到其他项目中，文件路径可能会发生变化
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            // 读取每一个资源文件
            for (URL resource : resources) {
                try {
                    InputStreamReader reader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] split = line.split("=");
                        if (split.length != 2) {
                            continue;
                        }
                        String key = split[0];
                        String className = split[1];
                        Class<?> aClass = Class.forName(className);
                        keyClassMap.put(key, aClass);
                    }
                } catch (Exception e) {
                    log.error("读取SPI配置文件失败", e);
                }
            }
            if (scanDir.equals(RPC_SYSTEM_SPI_DIR)) {
                log.info("加载系统SPI配置完成, 共加载 {} 个实现类", keyClassMap.size());
            } else {
                log.info("加载自定义SPI配置完成, 共加载 {} 个实现类", keyClassMap.size());
            }

        }
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

    public static <T> T getInstance(Class<T> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);
        if (keyClassMap == null) {
            throw new RuntimeException("SpiLoader未加载 " + tClassName + " 的SPI配置");
        }
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException("SpiLoader的 " + tClassName + " 未找到key为 " + key + " 的实现类");
        }
        // 获取要加载的实现类型
        Class<?> impClass = keyClassMap.get(key);
        // 从实例缓存中获取实例
        String impClassName = impClass.getName();
        if (!instanceCache.containsKey(impClassName)){
            synchronized (instanceCache){ // 双重检查,确保多线程环境下懒加载的安全性
                if (!instanceCache.containsKey(impClassName)){
                    try {
                        Object instance = impClass.newInstance();
                        instanceCache.put(impClassName, instance);
                    } catch (Exception e) {
                        throw new RuntimeException("SpiLoader实例化 " + impClassName + " 失败", e);
                    }
                }
            }
        }
        return (T) instanceCache.get(impClassName);
    }

}
