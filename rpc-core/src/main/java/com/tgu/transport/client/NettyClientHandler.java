package com.tgu.transport.client;

import com.tgu.pojo.RpcResponse;
import com.tgu.trace.TraceContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        // 心跳响应不需要处理
        if (response.getCode() == 200 && response.getMessage() != null
                && response.getMessage().contains("heartbeat")) {
            TraceContext.clear();
            log.info("收到心跳响应: {}, response: {}", ctx.channel().id(), response);
            return;
        }

//        log.info("接收服务端响应: requestId={}, channel={}", response.getRequestId(), ctx.channel().id());
        // 通过 requestId 完成对应的 Future，不再关闭 Channel
        UnprocessedRequests.complete(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端 Channel 异常: {}", cause.getMessage());
        ChannelProvider.remove(ctx.channel());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("客户端 Channel 断开: {}", ctx.channel().id());
        ChannelProvider.remove(ctx.channel());
        super.channelInactive(ctx);
    }
}
