package com.tgu.provider;

import com.tgu.fault.ratelimit.RateLimitProvider;
import com.tgu.registry.ZKServiceRegister;
import com.tgu.registry.interfaces.ServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {

    private Map<String, Object> interfaceProvider;


    // 服务端的端口与地址
    private int port;

    private String host;

    private ServiceRegister serviceRegister;

    private RateLimitProvider rateLimitProvider;


    public ServiceProvider() {
        this.interfaceProvider = new HashMap<>();
    }


    public ServiceProvider(String host, int port) {
        this.host = host;
        this.port = port;
        this.serviceRegister = new ZKServiceRegister();
        this.interfaceProvider = new HashMap<>();
        this.rateLimitProvider = new RateLimitProvider();
    }


    public void provideServiceInterface(Object service, boolean canRetry) {
        String serviceName = service.getClass().getName();
        Class<?>[] interfaceName = service.getClass().getInterfaces();
        for (Class<?> aClass : interfaceName) {
            interfaceProvider.put(aClass.getName(), service);
            serviceRegister.register(aClass.getName(), new InetSocketAddress(host, port), canRetry);
        }
    }


    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }

    public RateLimitProvider getRateLimitProvider() {
        return rateLimitProvider;
    }


}
