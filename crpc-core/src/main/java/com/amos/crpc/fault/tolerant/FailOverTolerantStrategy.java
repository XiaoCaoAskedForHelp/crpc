package com.amos.crpc.fault.tolerant;

import cn.hutool.core.collection.CollUtil;
import com.amos.crpc.RpcApplication;
import com.amos.crpc.config.RpcConfig;
import com.amos.crpc.fault.retry.RetryStrategy;
import com.amos.crpc.fault.retry.RetryStrategyFactory;
import com.amos.crpc.loadbalancer.LoadBalancer;
import com.amos.crpc.loadbalancer.LoadBalancerFactory;
import com.amos.crpc.model.RpcRequest;
import com.amos.crpc.model.RpcResponse;
import com.amos.crpc.model.ServiceMetaInfo;
import com.amos.crpc.server.tcp.VertxTcpClient;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 转移到其他服务节点-容错策略
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        List<ServiceMetaInfo> serviceMetaInfoList = (List<ServiceMetaInfo>) context.get("serviceMetaInfoList");
        ServiceMetaInfo selectedServiceMetaInfo = (ServiceMetaInfo) context.get("selectedServiceMetaInfo");

        // 移除失败节点
        removeFailedNode(serviceMetaInfoList, selectedServiceMetaInfo);

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getLoadBalancer(rpcConfig.getLoadBalancer());
        // 将调用方法名（请求路径） 作为负载均衡的参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        // 发动tcp请求
        RpcResponse rpcResponse = null;
        while (serviceMetaInfoList.size() > 0 && rpcResponse != null) {
            ServiceMetaInfo currentServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
            log.info("当前服务节点：{}", currentServiceMetaInfo);
            try {
                RetryStrategy retryStrategy = RetryStrategyFactory.getRetryStrategy(rpcConfig.getRetryStrategy());
                rpcResponse = retryStrategy.doRetry(() -> {
                    return VertxTcpClient.doRequest(rpcRequest, currentServiceMetaInfo);
                });
                return rpcResponse;
            } catch (Exception exception) {
                // 移除失败节点
                removeFailedNode(serviceMetaInfoList, currentServiceMetaInfo);
                continue;
            }
        }
        throw new RuntimeException(e);
    }

    /**
     * 移除失败节点，可考虑下线
     *
     * @param serviceMetaInfoList
     * @param selectedServiceMetaInfo
     */
    private void removeFailedNode(List<ServiceMetaInfo> serviceMetaInfoList, ServiceMetaInfo selectedServiceMetaInfo) {
        if (CollUtil.isNotEmpty(serviceMetaInfoList)) {
            serviceMetaInfoList.remove(selectedServiceMetaInfo);
        }
    }
}
