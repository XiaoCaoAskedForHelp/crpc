package com.amos.crpc.LoadBalancer;

import com.amos.crpc.loadbalancer.ConsistentHashLoadBalancer;
import com.amos.crpc.loadbalancer.LoadBalancer;
import com.amos.crpc.loadbalancer.RandomLoadBalancer;
import com.amos.crpc.loadbalancer.RoundRobinLoadBalancer;
import com.amos.crpc.model.ServiceMetaInfo;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡器测试
 */
public class LoadBalancerTest {
//    final LoadBalancer loadBalancer = new ConsistentHashLoadBalancer();
//    final LoadBalancer loadBalancer = new RandomLoadBalancer();
    final LoadBalancer loadBalancer = new RoundRobinLoadBalancer();
    @Test
    public void select() {
        // 请求参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("name", "fdsafdsa");
        // 服务列表
        ServiceMetaInfo serviceMetaInfo1 = new ServiceMetaInfo();
        serviceMetaInfo1.setServiceName("myService1");
        serviceMetaInfo1.setServiceVersion("1.0.0");
        serviceMetaInfo1.setServiceHost("localhost");
        serviceMetaInfo1.setServicePort(8080);
        ServiceMetaInfo serviceMetaInfo2 = new ServiceMetaInfo();
        serviceMetaInfo2.setServiceName("myService2");
        serviceMetaInfo2.setServiceVersion("1.0.0");
        serviceMetaInfo2.setServiceHost("localhost");
        serviceMetaInfo2.setServicePort(8081);
        List<ServiceMetaInfo> serviceMetaInfoList = List.of(serviceMetaInfo1, serviceMetaInfo2);
        // 连续调用三次
        for (int i = 0; i < 3; i++) {
            ServiceMetaInfo serviceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
            System.out.println(serviceMetaInfo);
        }
    }
}
