package com.tgu.transport.server;

import com.tgu.config.RpcConfig;
import com.tgu.transport.codec.CustomDecoder;
import com.tgu.transport.codec.CustomEncoder;
import com.tgu.provider.ServiceProvider;
import com.tgu.serializers.Serializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private ServiceProvider serviceProvider;

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
//        pipeline.addLast(new LengthFieldPrepender(4));
        
        // 根据配置获取序列化器
        Serializer serializer = Serializer.getSerializerByCode(RpcConfig.getSerializerCode());
        log.info("服务端使用序列化器: {}", RpcConfig.getSerializerType());
        
        pipeline.addLast(new CustomDecoder());
        pipeline.addLast(new CustomEncoder(serializer));
        pipeline.addLast(new NettyServerHandler(serviceProvider));
    }
}
