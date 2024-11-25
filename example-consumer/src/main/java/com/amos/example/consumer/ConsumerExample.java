package com.amos.example.consumer;

import com.amos.crpc.bootstrap.ConsumerBootstrap;
import com.amos.crpc.proxy.ServiceProxyFactory;
import com.amos.crpc.serializer.Serializer;
import com.amos.crpc.spi.SpiLoader;
import com.amos.example.common.model.User;
import com.amos.example.common.service.UserService;

/**
 * 服务消费者示例
 */
public class ConsumerExample {
//    public static void main(String[] args) {
//        RpcConfig rpcConfig = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
//        System.out.println(rpcConfig);
//    }

    public static void main(String[] args) {
        // 服务提供者初始化
        ConsumerBootstrap.init();
        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("fdsahfdsa 你发的撒fdsaf");
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }

        user = new User();
        user.setName("2222222222222");
        newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        user = new User();
        user.setName("333333333");
        newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
//        long number = userService.getNumber();
//        System.out.println(number);

    }
}
