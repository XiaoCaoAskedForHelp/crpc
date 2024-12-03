package com.amos.crpc.proxy;

import cn.hutool.core.collection.CollUtil;
import com.amos.crpc.RpcApplication;
import com.amos.crpc.config.RpcConfig;
import com.amos.crpc.constant.RpcConstant;
import com.amos.crpc.fault.retry.RetryStrategy;
import com.amos.crpc.fault.retry.RetryStrategyFactory;
import com.amos.crpc.loadbalancer.LoadBalancer;
import com.amos.crpc.loadbalancer.LoadBalancerFactory;
import com.amos.crpc.model.RpcRequest;
import com.amos.crpc.model.RpcResponse;
import com.amos.crpc.model.ServiceMetaInfo;
import com.amos.crpc.registry.Registry;
import com.amos.crpc.registry.RegistryFactory;
import com.amos.crpc.serializer.Serializer;
import com.amos.crpc.serializer.SerializerFactory;
import com.amos.crpc.server.tcp.VertxTcpClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态代理
 * 根据要生成的对象的类型，自动生成一个代码里对象
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
//        Serializer serializer = new JDKSerializer();
        Serializer serializer = SerializerFactory.getSerializer(RpcApplication.getRpcConfig().getSerializer());

        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        // 从注册中心获取服务提供者请求地址
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getRegistry(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(rpcRequest.getServiceName());
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("未找到服务提供者");
        }
        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getLoadBalancer(rpcConfig.getLoadBalancer());
        // 将调用方法名（请求路径） 作为负载均衡的参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        log.info("负载均衡器: {}, 选择的服务提供者: {}", loadBalancer.getClass().getSimpleName(), selectedServiceMetaInfo);

        // http请求
//        try {
//            // 序列化
//            byte[] bodyBytes = serializer.serialize(rpcRequest);
//            // 发动请求
//            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
//                    .body(bodyBytes)
//                    .execute()) {
//                byte[] result = httpResponse.bodyBytes();
//                // 反序列化
//                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
//                return rpcResponse.getData();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        // 发动tcp请求
        try {
            RetryStrategy retryStrategy = RetryStrategyFactory.getRetryStrategy(rpcConfig.getRetryStrategy());
            RpcResponse rpcResponse = retryStrategy.doRetry(() -> {
//                throw new RuntimeException("请求失败");  # 测试重试策略
                return VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            });
            return rpcResponse.getData();
        } catch (Exception e) {
            throw new RuntimeException("请求失败");
        }
    }
}
