package com.amos.crpc.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.yaml.YamlUtil;
import com.amos.crpc.constant.RpcConstant;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 配置工具类
 */
public class ConfigUtils {
    /**
     * 加载配置对象
     *
     * @param tClass
     * @param prefix
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "", RpcConstant.DEFAULT_CONFIG_FILE_SUFFIX);
    }

    /**
     * 加载配置对象，支持区分环境，支持区分配置文件类型
     *
     * @param tClass
     * @param prefix
     * @param environment
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment, String fileSuffix) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(fileSuffix);
        if (".properties".equals(fileSuffix)) {
            Props props = new Props(configFileBuilder.toString());
            return props.toBean(tClass, prefix);
        } else if (".yml".equals(fileSuffix)) {
            try (InputStream inputStream = ResourceUtil.getStream(configFileBuilder.toString());
                 InputStreamReader reader = new InputStreamReader(inputStream)) {

                // 加载 YAML 文件为 Map
                Map<String, Object> yamlMap = YamlUtil.load(reader);

                // 如果有前缀，提取子配置
                if (StrUtil.isNotBlank(prefix)) {
                    String[] prefixKeys = prefix.split("\\.");
                    for (String key : prefixKeys) {
                        Object subMap = yamlMap.get(key);
                        if (subMap instanceof Map) {
                            yamlMap = (Map<String, Object>) subMap;
                        } else {
                            throw new RuntimeException("YAML 配置中未找到对应的前缀: " + prefix);
                        }
                    }
                }

                // 将 Map 转换为目标对象
                return BeanUtil.toBean(yamlMap, tClass);
            } catch (Exception e) {
                throw new RuntimeException("加载 YAML 文件失败: " + configFileBuilder.toString(), e);
            }
        } else {
            throw new RuntimeException("不支持的配置文件类型");
        }

    }
}
