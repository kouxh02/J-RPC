package com.tgu.transport.client;

import com.tgu.constants.Constant;
import com.tgu.pojo.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        AttributeKey<RpcResponse> key = AttributeKey.valueOf(Constant.RPC_RESPONSE);
        log.info("接收服务端返回的响应数据，将其暂存在 Channel >>> {}", ctx.channel().id());
        ctx.channel().attr(key).set(response);
        ctx.channel().close();
    }
}
