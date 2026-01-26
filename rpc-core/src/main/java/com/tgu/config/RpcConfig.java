package com.tgu.config;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RPC配置类 - 从application.yml读取配置
 */
@Slf4j
public class RpcConfig {

    private static final Map<String, Object> config;

    static {
        Yaml yaml = new Yaml();
        InputStream inputStream = RpcConfig.class.getClassLoader().getResourceAsStream("application.yml");
        if (inputStream == null) {
            log.warn("未找到application.yml配置文件，将使用默认配置");
            config = Map.of();
        } else {
            config = yaml.load(inputStream);
            log.info("成功加载配置文件: application.yml");
        }
    }

    // ==================== zipkin配置 ====================

    public static String getZipkinUrl() {
        return getConfigValue("zipkin.url", "http://172.31.151.142:9411/api/v2/spans");
    }

    // ==================== ZooKeeper配置 ====================

    public static String getZkHost() {
        return getConfigValue("zookeeper.host", "172.31.151.142");
    }

    public static int getZkPort() {
        return getConfigValue("zookeeper.port", 2181);
    }

    public static String getZkConnectString() {
        return getZkHost() + ":" + getZkPort();
    }

    public static String getZkRootPath() {
        return getConfigValue("zookeeper.root-path", "RPC");
    }

    public static String getZkRetryPath() {
        return getConfigValue("zookeeper.retry-path", "CanRetry");
    }

    public static int getZkSessionTimeout() {
        return getConfigValue("zookeeper.session-timeout", 40000);
    }

    public static int getZkConnectionTimeout() {
        return getConfigValue("zookeeper.connection-timeout", 15000);
    }

    // ==================== 服务端配置 ====================

    public static String getServerHost() {
        return getConfigValue("server.host", "127.0.0.1");
    }

    public static int getServerPort() {
        return getConfigValue("server.port", 9999);
    }

    // ==================== 客户端配置 ====================

    public static String getLoadBalanceStrategy() {
        return getConfigValue("client.load-balance", "round");
    }

    // ==================== 序列化配置 ====================

    public static String getSerializerType() {
        return getConfigValue("serializer.type", "json");
    }

    public static int getSerializerCode() {
        String type = getSerializerType();
        return switch (type.toLowerCase()) {
            case "object" -> 0;
            case "json" -> 1;
            case "protostuff" -> 2;
            default -> 1;
        };
    }

    // ==================== 心跳配置 ====================

    /**
     * 服务端读空闲超时时间（秒）
     * 超过此时间没有收到客户端心跳，触发读空闲事件
     */
    public static int getServerReaderIdleTime() {
        return getConfigValue("heartbeat.server.reader-idle-time", 10);
    }

    /**
     * 服务端写空闲超时时间（秒）
     * 超过此时间没有写数据，触发写空闲事件
     */
    public static int getServerWriterIdleTime() {
        return getConfigValue("heartbeat.server.writer-idle-time", 20);
    }

    /**
     * 客户端写空闲超时时间（秒）
     * 超过此时间没有写数据，触发写空闲事件，发送心跳包
     */
    public static int getClientWriterIdleTime() {
        return getConfigValue("heartbeat.client.writer-idle-time", 8);
    }

    // ==================== 限流配置 ====================

    public static boolean isRateLimitEnabled() {
        return getConfigValue("fault-tolerance.rate-limit.enabled", true);
    }

    public static int getRateLimitRate() {
        return getConfigValue("fault-tolerance.rate-limit.rate", 200);
    }

    public static int getRateLimitCapacity() {
        return getConfigValue("fault-tolerance.rate-limit.capacity", 3);
    }

    // ==================== 重试配置 ====================

    public static boolean isRetryEnabled() {
        return getConfigValue("fault-tolerance.retry.enabled", true);
    }

    public static int getRetryMaxAttempts() {
        return getConfigValue("fault-tolerance.retry.max-attempts", 3);
    }

    public static int getRetryWaitTime() {
        return getConfigValue("fault-tolerance.retry.wait-time", 700);
    }

    public static TimeUnit getRetryWaitTimeUnit() {
        String unit = getConfigValue("fault-tolerance.retry.wait-time-unit", "MILLISECONDS");
        return TimeUnit.valueOf(unit.toUpperCase());
    }

    // ==================== 熔断器配置 ====================

    public static boolean isCircuitBreakerEnabled() {
        return getConfigValue("fault-tolerance.circuit-breaker.enabled", true);
    }

    public static int getCircuitBreakerFailureThreshold() {
        return getConfigValue("fault-tolerance.circuit-breaker.failure-threshold", 2);
    }

    public static double getCircuitBreakerHalfOpenSuccessRate() {
        Object value = getNestedValue("fault-tolerance.circuit-breaker.half-open-success-rate");
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.4;
    }

    public static long getCircuitBreakerRestTimePeriod() {
        return getConfigValue("fault-tolerance.circuit-breaker.rest-time-period", 3000);
    }

    // ==================== 服务配置 ====================

    public static boolean isServiceCanRetry() {
        return getConfigValue("service.can-retry", false);
    }

    // ==================== 工具方法 ====================

    @SuppressWarnings("unchecked")
    private static <T> T getConfigValue(String key, T defaultValue) {
        try {
            Object value = getNestedValue(key);
            if (value == null) {
                return defaultValue;
            }
            // 类型转换
            if (defaultValue instanceof Integer && value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
            if (defaultValue instanceof Long && value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            }
            if (defaultValue instanceof Boolean && value instanceof Boolean) {
                return (T) value;
            }
            if (defaultValue instanceof String && value instanceof String) {
                return (T) value;
            }
            return (T) value;
        } catch (Exception e) {
            log.warn("获取配置项 {} 失败，使用默认值: {}", key, defaultValue);
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object getNestedValue(String key) {
        String[] keys = key.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < keys.length - 1; i++) {
            Object value = current.get(keys[i]);
            if (value instanceof Map) {
                current = (Map<String, Object>) value;
            } else {
                return null;
            }
        }

        return current.get(keys[keys.length - 1]);
    }
}
