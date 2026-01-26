package com.tgu.transport.client;

import com.tgu.pojo.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未完成请求管理器
 * 使用 requestId -> CompletableFuture 映射来匹配请求和响应
 */
@Slf4j
public class UnprocessedRequests {

    private static final Map<String, CompletableFuture<RpcResponse>> FUTURES = new ConcurrentHashMap<>();

    /**
     * 注册请求，返回 Future 用于等待响应
     */
    public static CompletableFuture<RpcResponse> put(String requestId) {
        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        FUTURES.put(requestId, future);
        return future;
    }

    /**
     * 完成请求，设置响应结果
     */
    public static void complete(RpcResponse response) {
        String requestId = response.getRequestId();
        CompletableFuture<RpcResponse> future = FUTURES.remove(requestId);
        if (future != null) {
            future.complete(response);
            log.debug("完成请求: requestId={}", requestId);
        } else {
            log.warn("未找到对应的请求: requestId={}", requestId);
        }
    }

    /**
     * 移除请求（超时或异常时调用）
     */
    public static void remove(String requestId) {
        FUTURES.remove(requestId);
    }
}
