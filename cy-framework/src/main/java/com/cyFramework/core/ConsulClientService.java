package com.cyFramework.core;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConsulClientService {

    private final DiscoveryClient discoveryClient;

    public ConsulClientService(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public String getServiceUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        return instances.get(0).getUri().toString();
    }
}
