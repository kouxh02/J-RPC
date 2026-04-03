package com.tgu.transport.server;

import com.tgu.config.RpcConfig;
import com.tgu.transport.codec.CustomDecoder;
import com.tgu.transport.codec.CustomEncoder;
import com.tgu.provider.ServiceProvider;
import com.tgu.serializers.Serializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private ServiceProvider serviceProvider;

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
        // pipeline.addLast(new LengthFieldPrepender(4));

        // 根据配置获取序列化器
        Serializer serializer = Serializer.getSerializerByName(RpcConfig.getSerializerType());
        if (serializer == null) {
            throw new IllegalStateException("未找到对应的序列化器: " + RpcConfig.getSerializerType());
        }
//        log.info("服务端使用序列化器: {}", RpcConfig.getSerializerType());

        // 读空闲/写空闲超时处理
        int readerIdleTime = RpcConfig.getServerReaderIdleTime();
        int writerIdleTime = RpcConfig.getServerWriterIdleTime();


        // 出站处理器顺序：编码器 -> 心跳检测 -> 业务处理器
        // 入站处理器顺序：心跳检测 -> 解码器 -> 业务处理器
        pipeline.addLast(new CustomDecoder());
        pipeline.addLast(new CustomEncoder(serializer));

//        log.info("服务端心跳配置: readerIdleTime={}s, writerIdleTime={}s", readerIdleTime, writerIdleTime);
        pipeline.addLast(new IdleStateHandler(readerIdleTime, writerIdleTime, 0));
        pipeline.addLast(new ServerHeartBeatHandler());

        pipeline.addLast(new NettyServerHandler(serviceProvider));
    }
}
