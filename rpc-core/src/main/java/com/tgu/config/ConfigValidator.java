package com.tgu.config;

import com.tgu.loadbalance.LoadBalance;
import com.tgu.serializers.Serializer;
import lombok.extern.slf4j.Slf4j;

/**
 * 配置验证工具类 - 用于验证配置是否正确加载
 */
@Slf4j
public class ConfigValidator {

    /**
     * 验证配置是否有效
     * @return true表示配置有效，false表示配置无效
     */
    public static boolean validateConfig() {
        try {
            // 验证ZooKeeper配置
            if (RpcConfig.getZkHost() == null || RpcConfig.getZkHost().isEmpty()) {
                log.error("ZooKeeper主机地址未配置");
                return false;
            }
            if (RpcConfig.getZkPort() <= 0 || RpcConfig.getZkPort() > 65535) {
                log.error("ZooKeeper端口配置无效: {}", RpcConfig.getZkPort());
                return false;
            }
            
            // 验证服务端配置
            if (RpcConfig.getServerPort() <= 0 || RpcConfig.getServerPort() > 65535) {
                log.error("服务端口配置无效: {}", RpcConfig.getServerPort());
                return false;
            }

            if (RpcConfig.getClientRequestTimeout() <= 0) {
                log.error("客户端请求超时配置无效: {}", RpcConfig.getClientRequestTimeout());
                return false;
            }

            if (Serializer.getSerializerByName(RpcConfig.getSerializerType()) == null) {
                log.error("未找到对应的序列化器: {}", RpcConfig.getSerializerType());
                return false;
            }

            if (LoadBalance.getLoadBalance(RpcConfig.getLoadBalanceStrategy()) == null) {
                log.error("未找到对应的负载均衡策略: {}", RpcConfig.getLoadBalanceStrategy());
                return false;
            }

            if (RpcConfig.isTracingEnabled() &&
                    (RpcConfig.getTracingServiceName() == null || RpcConfig.getTracingServiceName().isEmpty())) {
                log.error("链路追踪服务名未配置");
                return false;
            }
            
            // 验证限流配置
            if (RpcConfig.getRateLimitRate() <= 0) {
                log.error("限流速率配置无效: {}", RpcConfig.getRateLimitRate());
                return false;
            }
            if (RpcConfig.getRateLimitCapacity() <= 0) {
                log.error("令牌桶容量配置无效: {}", RpcConfig.getRateLimitCapacity());
                return false;
            }
            
            // 验证重试配置
            if (RpcConfig.getRetryMaxAttempts() <= 0) {
                log.error("重试次数配置无效: {}", RpcConfig.getRetryMaxAttempts());
                return false;
            }
            if (RpcConfig.getRetryWaitTime() < 0) {
                log.error("重试等待时间配置无效: {}", RpcConfig.getRetryWaitTime());
                return false;
            }
            
            // 验证熔断器配置
            if (RpcConfig.getCircuitBreakerFailureThreshold() <= 0) {
                log.error("熔断器失败阈值配置无效: {}", RpcConfig.getCircuitBreakerFailureThreshold());
                return false;
            }
            if (RpcConfig.getCircuitBreakerHalfOpenSuccessRate() < 0 || 
                RpcConfig.getCircuitBreakerHalfOpenSuccessRate() > 1) {
                log.error("熔断器半开成功率配置无效: {}", RpcConfig.getCircuitBreakerHalfOpenSuccessRate());
                return false;
            }
            if (RpcConfig.getCircuitBreakerRestTimePeriod() <= 0) {
                log.error("熔断器恢复时间配置无效: {}", RpcConfig.getCircuitBreakerRestTimePeriod());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("配置验证失败: {}", e.getMessage());
            return false;
        }
    }
}
