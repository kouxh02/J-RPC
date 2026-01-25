package com.tgu.transport.server;

import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;
import com.tgu.provider.ServiceProvider;
import com.tgu.fault.ratelimit.RateLimit;
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
        log.info("开始处理客户端请求: {}", rpcRequest);
        RpcResponse response = getResponse(rpcRequest);
        // TimeUnit.SECONDS.sleep(2);
        ctx.writeAndFlush(response);
        log.info("请求处理完成，返回响应: {}", response);
        ctx.close();
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
