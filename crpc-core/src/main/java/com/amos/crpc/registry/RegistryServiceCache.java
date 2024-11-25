package com.amos.crpc.registry;


import com.amos.crpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心服务本地缓存
 */
public class RegistryServiceCache {
    /**
     * 服务缓存
     */
    List<ServiceMetaInfo> serviceMetaInfoList;

    /**
     * 写缓存
     *
     * @param serviceMetaInfoList
     */
    void writeCache(List<ServiceMetaInfo> serviceMetaInfoList) {
        this.serviceMetaInfoList = serviceMetaInfoList;
    }


    /**
     * 读缓存
     *
     * @return
     */
    List<ServiceMetaInfo> readCache() {
        return serviceMetaInfoList;
    }

    /**
     * 清空缓存
     */
    void clearCache() {
        serviceMetaInfoList = null;
    }
}
