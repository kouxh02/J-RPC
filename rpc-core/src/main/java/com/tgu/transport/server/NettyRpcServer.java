package com.tgu.transport.server;

import com.tgu.provider.ServiceProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcServer implements RpcServer {


    private final ServiceProvider serviceProvider;
    private ChannelFuture channelFuture;

    public NettyRpcServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));
            channelFuture = bootstrap.bind(port).sync();
            log.info(">>> 服务端启动");

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("NettyRpcServer error >>> {}", e.getMessage());
        } finally {
            shutdown(bossGroup, workGroup);
        }

    }

    @Override
    public void stop() {
        if (channelFuture != null) {
            try {
                channelFuture.channel().closeFuture().sync();
                log.info(">>> 服务端主通道已关闭");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("关闭Netty服务端主通道时终端");
            }
        }
    }

    private void shutdown(NioEventLoopGroup bossGroup, NioEventLoopGroup workGroup) {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }

        if (workGroup != null) {
            workGroup.shutdownGracefully().syncUninterruptibly();
        }
    }
}
