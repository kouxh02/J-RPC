package com.tgu.transport.server;

import com.tgu.enums.RequestType;
import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;
import com.tgu.provider.ServiceProvider;
import com.tgu.fault.ratelimit.RateLimit;
import com.tgu.trace.ServerTraceInterceptor;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@AllArgsConstructor
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private ServiceProvider serviceProvider;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        if (rpcRequest == null) {
            log.error("接收到非法请求，RpcRequest 为空");
            return;
        }
        if (rpcRequest.getType() == RequestType.HEARTBEAT) {
            log.info("接收到来自客户端的心跳包: {}", ctx.channel().id());
            // 返回心跳响应
            RpcResponse heartbeatResponse = RpcResponse.builder()
                    .requestId(rpcRequest.getRequestId())
                    .code(200)
                    .message("heartbeat ok")
                    .build();
            ctx.writeAndFlush(heartbeatResponse);
            return;
        }
        if (rpcRequest.getType() == RequestType.NORMAL) {
            ServerTraceInterceptor.beforeHandle();
            log.info("开始处理客户端请求: requestId={}", rpcRequest.getRequestId());
            RpcResponse response = getResponse(rpcRequest);
            response.setRequestId(rpcRequest.getRequestId());
            ctx.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
                try {
                    if (future.isSuccess()) {
                        log.info("服务端返回响应: requestId={}, code={}", rpcRequest.getRequestId(), response.getCode());
                    } else {
                        log.error("服务端返回响应失败: requestId={}", rpcRequest.getRequestId(), future.cause());
                    }
                } finally {
                    ServerTraceInterceptor.afterHandle(rpcRequest.getMethodName());
                }
            });
        }
    }

    private RpcResponse getResponse(RpcRequest request) {
        String interfaceName = request.getInterfaceName();
        RateLimit rateLimit = serviceProvider.getRateLimitProvider().getRatelimit(interfaceName);
        if (!rateLimit.getToken()) {
            log.error("请求被限流拦截 - 接口: {}", interfaceName);
            return RpcResponse.fail();
        }
        Object service = serviceProvider.getService(interfaceName);
        Method method = null;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsType());
            Object data = method.invoke(service, request.getParams());
            return RpcResponse.sussess(data);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("NettyServerHandler error >>>>>> {}", e.getMessage());
            return RpcResponse.fail();
        }
    }

}
