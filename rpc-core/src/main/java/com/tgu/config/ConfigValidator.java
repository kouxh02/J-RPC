package com.tgu.config;

import lombok.extern.slf4j.Slf4j;

/**
 * 配置验证工具类 - 用于验证配置是否正确加载
 */
@Slf4j
public class ConfigValidator {

    /**
     * 打印所有配置信息
     */
    public static void printAllConfig() {
        log.info("========== RPC配置信息 ==========");
        
        log.info("【ZooKeeper配置】");
        log.info("  - 地址: {}:{}", RpcConfig.getZkHost(), RpcConfig.getZkPort());
        log.info("  - 连接字符串: {}", RpcConfig.getZkConnectString());
        log.info("  - 根路径: {}", RpcConfig.getZkRootPath());
        log.info("  - 重试路径: {}", RpcConfig.getZkRetryPath());
        log.info("  - 会话超时: {}ms", RpcConfig.getZkSessionTimeout());
        log.info("  - 连接超时: {}ms", RpcConfig.getZkConnectionTimeout());
        
        log.info("【服务端配置】");
        log.info("  - 主机: {}", RpcConfig.getServerHost());
        log.info("  - 端口: {}", RpcConfig.getServerPort());
        log.info("  - 默认支持重试: {}", RpcConfig.isServiceCanRetry());
        
        log.info("【客户端配置】");
        log.info("  - 负载均衡策略: {}", RpcConfig.getLoadBalanceStrategy());
        
        log.info("【序列化配置】");
        log.info("  - 序列化类型: {} (code: {})", RpcConfig.getSerializerType(), RpcConfig.getSerializerCode());
        
        log.info("【限流配置】");
        log.info("  - 启用状态: {}", RpcConfig.isRateLimitEnabled());
        log.info("  - 令牌生成速率: {}ms/个", RpcConfig.getRateLimitRate());
        log.info("  - 令牌桶容量: {}", RpcConfig.getRateLimitCapacity());
        
        log.info("【重试配置】");
        log.info("  - 启用状态: {}", RpcConfig.isRetryEnabled());
        log.info("  - 最大重试次数: {}", RpcConfig.getRetryMaxAttempts());
        log.info("  - 重试等待时间: {} {}", RpcConfig.getRetryWaitTime(), RpcConfig.getRetryWaitTimeUnit());
        
        log.info("【熔断器配置】");
        log.info("  - 启用状态: {}", RpcConfig.isCircuitBreakerEnabled());
        log.info("  - 失败阈值: {}", RpcConfig.getCircuitBreakerFailureThreshold());
        log.info("  - 半开状态成功率: {}", RpcConfig.getCircuitBreakerHalfOpenSuccessRate());
        log.info("  - 恢复时间: {}ms", RpcConfig.getCircuitBreakerRestTimePeriod());
        
        log.info("===================================");
    }
    
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
            
            log.info("配置验证通过 ✓");
            return true;
        } catch (Exception e) {
            log.error("配置验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 主函数 - 用于独立测试配置加载
     */
    public static void main(String[] args) {
        log.info("开始验证RPC配置...");
        printAllConfig();
        
        if (validateConfig()) {
            log.info("配置验证成功，所有配置项均正常");
        } else {
            log.error("配置验证失败，请检查配置文件");
            System.exit(1);
        }
    }
}
