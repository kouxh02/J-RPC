package com.tgu.transport.client;

import com.tgu.config.RpcConfig;
import com.tgu.transport.codec.CustomDecoder;
import com.tgu.transport.codec.CustomEncoder;
import com.tgu.serializers.Serializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
//        pipeline.addLast(new LengthFieldBasedFrameDecoder(
//                Integer.MAX_VALUE,
//                0,
//                4,
//                0,
//                4
//        ));
//        // 计算当前待发送消息的长度，写入到前4个字节中
//        pipeline.addLast(new LengthFieldPrepender(4));
        
        // 根据配置获取序列化器
        Serializer serializer = Serializer.getSerializerByCode(RpcConfig.getSerializerCode());
        log.info("客户端使用序列化器: {}", RpcConfig.getSerializerType());
        
        pipeline.addLast(new CustomEncoder(serializer));
        pipeline.addLast(new CustomDecoder());
        pipeline.addLast(new NettyClientHandler());
    }
}
