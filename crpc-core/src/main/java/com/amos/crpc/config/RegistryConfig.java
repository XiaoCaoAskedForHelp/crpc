package com.amos.crpc.config;

import lombok.Data;

/**
 * RPC注册中心配置
 */
@Data
public class RegistryConfig {
    /**
     * 注册中心类别
     */
    private String registry = "etcd";

    /**
     * 注册中心地址
     */
    private String address = "http://localhost:2379";

    /**
     * 注册中心用户名
     */
    private String username;

    /**
     * 注册中心密码
     */
    private String password;

    /**
     * 注册中心超时时间(单位毫秒)
     */
    private Long timeout = 10000L;
}
