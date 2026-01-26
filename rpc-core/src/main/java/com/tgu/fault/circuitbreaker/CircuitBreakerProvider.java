package com.tgu.fault.circuitbreaker;

import com.tgu.config.RpcConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CircuitBreakerProvider {

    private Map<String, CircuitBreaker> circuitBreakerMap = new HashMap<>();

    public synchronized CircuitBreaker getCircuitBreaker(String serviceName) {
        CircuitBreaker circuitBreaker;
        if (circuitBreakerMap.containsKey(serviceName)) {
            circuitBreaker = circuitBreakerMap.get(serviceName);
        } else {
//            log.info("service >>> {}, 创建一个新的熔断器", serviceName);
            // 从配置文件读取熔断器参数
            int failureThreshold = RpcConfig.getCircuitBreakerFailureThreshold();
            double halfOpenSuccessRate = RpcConfig.getCircuitBreakerHalfOpenSuccessRate();
            long restTimePeriod = RpcConfig.getCircuitBreakerRestTimePeriod();
            
//            log.info("熔断器配置 - 失败阈值: {}, 半开成功率: {}, 恢复时间: {}ms", failureThreshold, halfOpenSuccessRate, restTimePeriod);
            
            circuitBreaker = new CircuitBreaker(failureThreshold, halfOpenSuccessRate, restTimePeriod);
            circuitBreakerMap.put(serviceName, circuitBreaker);
        }
        return circuitBreaker;
    }

}
