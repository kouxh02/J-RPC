package com.tgu.fault.retry;

import com.github.rholder.retry.*;
import com.tgu.config.RpcConfig;
import com.tgu.transport.client.RpcClient;
import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GuavaRetry {

    private RpcClient rpcClient;

    public RpcResponse sendServiceWithRetry(RpcRequest request, RpcClient rpcClient) {
        this.rpcClient = rpcClient;
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfException()
                // 重试会在请求发生异常或返回状态码为500时进行
                .retryIfResult(response -> Objects.equals(response.getCode(), 500))
                // 每次重试之间固定等待时间（从配置读取）
                .withWaitStrategy(WaitStrategies.fixedWait(
                        RpcConfig.getRetryWaitTime(), 
                        RpcConfig.getRetryWaitTimeUnit()))
                // 最多重试次数（从配置读取）
                .withStopStrategy(StopStrategies.stopAfterAttempt(RpcConfig.getRetryMaxAttempts()))
                // 用于为Retryer配置一个重试监听器
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        if (attempt.hasException()) {
                            log.error("RetryListener >>> 第 {} 次调用失败 - 异常: {}",
                                    attempt.getAttemptNumber(),
                                    attempt.getExceptionCause().getMessage());
                        } else if (attempt.hasResult()) {
                            log.error("RetryListener >>> 第 {} 次调用失败 - 返回码: {}",
                                    attempt.getAttemptNumber(),
                                    ((RpcResponse) attempt.getResult()).getCode());
                        }
                    }
                })
                .build();
        try {
            return retryer.call(() -> rpcClient.sendRequest(request));
        } catch (ExecutionException | RetryException e) {
            log.error("重试机制最终失败 - 接口: {}, 方法: {}, 异常: {}",
                    request.getInterfaceName(),
                    request.getMethodName(),
                    e.getMessage());
        }
        return RpcResponse.fail();
    }

}
