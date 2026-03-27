package com.tgu.fault.ratelimit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenBucketRateLimitImpl implements RateLimit {

    private final int rate; // 令牌产生速率（单位：ms/个，即每多少毫秒生成一个）
    private final int capacity; // 桶容量

    private volatile int curCapacity; // 当前桶内令牌数
    private long lastRefillTime; // 上次补充令牌的时间戳

    /**
     * @param rate     生产速率：每隔多少毫秒生产一个令牌
     * @param capacity 桶的最大容量
     */
    public TokenBucketRateLimitImpl(int rate, int capacity) {
        this.rate = rate;
        this.capacity = capacity;
        this.curCapacity = capacity; // 默认初始化时桶是满的
        this.lastRefillTime = System.currentTimeMillis();
    }

    @Override
    public synchronized boolean getToken() {
        // 1. 惰性填充：先根据时间差补充令牌
        refill();

        // 2. 尝试消费令牌
        if (curCapacity > 0) {
            curCapacity--;
            log.info("令牌获取成功 - 剩余令牌: {}, 线程: {}", curCapacity, Thread.currentThread().getName());
            return true;
        }

        // 3. 无令牌可消费
        log.error("令牌获取失败 - 当前令牌: 0, 线程: {}", Thread.currentThread().getName());
        return false;
    }

    /**
     * 根据时间差补充令牌
     */
    private void refill() {
        long now = System.currentTimeMillis();

        // 距离上次补充过去了多久
        long duration = now - lastRefillTime;

        // 计算这段时间应该生成多少令牌
        int newTokens = (int) (duration / rate);

        if (newTokens > 0) {
            int oldCapacity = curCapacity;
            // 更新令牌数量，但不能超过最大容量
            curCapacity = Math.min(capacity, curCapacity + newTokens);

            log.info("令牌补充 - 时间间隔: {}ms, 新生成令牌: {}, 补充前: {}, 补充后: {}",
                    duration, newTokens, oldCapacity, curCapacity);

            // 更新时间戳
            // 注意：这里不用 now，而是用 (lastRefillTime + newTokens * rate)
            // 这样可以避免"时间漂移"，保留不足生成一个令牌的时间余数
            lastRefillTime += (long) newTokens * rate;
        }
    }
}