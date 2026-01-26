package com.tgu.transport.client;

import com.tgu.pojo.RpcRequest;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHeartBeatHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            IdleState idleState = idleStateEvent.state();
            if (idleState == IdleState.WRITER_IDLE) {
                log.info("客户端发送心跳包 >>> {}", ctx.channel().id());
                ctx.writeAndFlush(RpcRequest.heartBeat());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("心跳处理器异常: {}", cause.getMessage());
        ChannelProvider.remove(ctx.channel());
        ctx.close();
    }
}
