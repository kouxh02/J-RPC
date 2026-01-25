package com.tgu.fault.ratelimit;

import com.tgu.config.RpcConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitProvider {

    private Map<String, RateLimit> rateLimitMap = new ConcurrentHashMap<>();

    public RateLimit getRatelimit(String interfaceName) {
        // 使用 computeIfAbsent 确保线程安全，避免多个线程创建多个实例
        return rateLimitMap.computeIfAbsent(interfaceName,
                key -> new TokenBucketRateLimitImpl(
                        RpcConfig.getRateLimitRate(), 
                        RpcConfig.getRateLimitCapacity()));
    }

}
