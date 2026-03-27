package com.tgu.fault.circuitbreaker;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CircuitBreaker {
    // 当前状态
    private CircuitBreakerState state = CircuitBreakerState.CLOSED; // 熔断器初始状态
    private final AtomicInteger failureCount = new AtomicInteger(0); // 失败请求计数
    private final AtomicInteger successCount = new AtomicInteger(0); // 成功请求计数
    private final AtomicInteger requestCount = new AtomicInteger(0); // 请求总数

    private final int failureThreshold; // 失败阈值
    private final double halfOpenSuccessRate; // 半开状态下的成功率阈值
    private final long restTimePeriod; // 重置时间周期
    private long lastFailureTime = 0; // 最后一次失败时间

    public CircuitBreaker(int failureThreshold, double halfOpenSuccessRate, long restTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.restTimePeriod = restTimePeriod;
    }

    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        switch (state) {
            case OPEN -> {
                if (currentTime - lastFailureTime > restTimePeriod) {
                    state = CircuitBreakerState.HALF_OPEN;
                    log.info("熔断器进入半开状态 - 开始试探性恢复");
                    resetCounts();
                    requestCount.incrementAndGet(); // 第一个进入HALF_OPEN的请求也要计数
                    return true;
                }
                return false;
            }
            case HALF_OPEN -> {
                requestCount.incrementAndGet();
                return true;
            }
            case CLOSED -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public synchronized void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            successCount.incrementAndGet();
            if (successCount.get() >= halfOpenSuccessRate * requestCount.get()) {
                state = CircuitBreakerState.CLOSED;// 恢复正常状态
                // 在重置前保存计数值用于日志
                int finalSuccessCount = successCount.get();
                int finalRequestCount = requestCount.get();
                double successRate = finalRequestCount > 0
                        ? (finalSuccessCount * 100.0 / finalRequestCount)
                        : 0;

                log.info("熔断器关闭 - 已恢复正常，成功率: {}/{} = {}%",
                        finalSuccessCount, finalRequestCount, String.format("%.1f", successRate));
                resetCounts();
            }
        }
    }

    public synchronized void recordFailure() {
        failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();

        if (state == CircuitBreakerState.HALF_OPEN) {
            state = CircuitBreakerState.OPEN;
            log.warn("熔断器打开 - HALF_OPEN状态下失败，failureCount: {}", failureCount.get());
        } else if (failureCount.get() >= failureThreshold) {
            state = CircuitBreakerState.OPEN;
            log.warn("熔断器打开 - 失败次数达到阈值，failureCount: {}, threshold: {}",
                    failureCount.get(), failureThreshold);
        }
    }

    private void resetCounts() {
        failureCount.set(0);
        successCount.set(0);
        requestCount.set(0);
    }

}

enum CircuitBreakerState {
    // 关闭，开启，半开启
    CLOSED, OPEN, HALF_OPEN
}