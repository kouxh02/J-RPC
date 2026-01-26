package com.tgu.transport.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerHeartBeatHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        try {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                IdleState idleState = idleStateEvent.state();
                if (idleState == IdleState.READER_IDLE) {
                    log.warn("服务端读空闲超时，关闭连接: {}", ctx.channel().id());
                    ctx.close();
                } else if (idleState == IdleState.WRITER_IDLE) {
                    log.warn("服务端写空闲超时，关闭连接: {}", ctx.channel().id());
                    ctx.close();
                }
            }
        } catch (Exception e) {
            log.error("HeartBeatHandler 捕获异常: ", e);
            ctx.close();
        }
    }
}
