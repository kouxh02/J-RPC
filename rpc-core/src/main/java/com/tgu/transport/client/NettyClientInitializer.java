package com.tgu.transport.client;

import com.tgu.config.RpcConfig;
import com.tgu.transport.codec.CustomDecoder;
import com.tgu.transport.codec.CustomEncoder;
import com.tgu.serializers.Serializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // pipeline.addLast(new LengthFieldBasedFrameDecoder(
        // Integer.MAX_VALUE,
        // 0,
        // 4,
        // 0,
        // 4
        // ));
        // // 计算当前待发送消息的长度，写入到前4个字节中
        // pipeline.addLast(new LengthFieldPrepender(4));

        // 根据配置获取序列化器
        Serializer serializer = Serializer.getSerializerByName(RpcConfig.getSerializerType());
        if (serializer == null) {
            throw new IllegalStateException("未找到对应的序列化器: " + RpcConfig.getSerializerType());
        }
        // log.info("客户端使用序列化器: {}", RpcConfig.getSerializerType());

        pipeline.addLast(new CustomEncoder(serializer));
        pipeline.addLast(new CustomDecoder());

        // 心跳机制：必须在业务Handler之前，以便正确监控空闲状态
        int writerIdleTime = RpcConfig.getClientWriterIdleTime();
//        log.debug("客户端心跳配置: writerIdleTime={}s", writerIdleTime);

        // 0表示不监控读空闲，writerIdleTime表示写空闲时间，0表示不监控读写空闲
        pipeline.addLast(new IdleStateHandler(0, writerIdleTime, 0, TimeUnit.SECONDS));
        pipeline.addLast(new ClientHeartBeatHandler());

        pipeline.addLast(new NettyClientHandler());
    }
}
