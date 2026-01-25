package com.tgu.transport.client;

import com.tgu.constants.Constant;
import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;
import com.tgu.registry.interfaces.ServiceCenter;
import com.tgu.registry.ZKServiceCenter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


@AllArgsConstructor
@Slf4j
public class NettyZKRpcClient implements RpcClient {

    private String host;

    private int port;

    private static final EventLoopGroup eventLoopGroup;

    private static final Bootstrap bootstrap;


    private ServiceCenter serviceCenter;

    public NettyZKRpcClient () {
        this.serviceCenter = new ZKServiceCenter();
    }

    public NettyZKRpcClient (ServiceCenter serviceCenter) {
        this.serviceCenter = serviceCenter;
    }


    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }


    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) {
        try {
            InetSocketAddress inetSocketAddress = serviceCenter.serviceDiscovery(rpcRequest.getInterfaceName());
            if (inetSocketAddress == null) {
                log.error("服务未注册，InterfaceName >>> {}", rpcRequest.getInterfaceName());
                return RpcResponse.fail();
            }
//            log.info("发现ZK服务 >>> {}", inetSocketAddress);
            ChannelFuture channelFuture = bootstrap.connect(inetSocketAddress.getAddress(), inetSocketAddress.getPort()).sync();
//            log.info("客户端：链接建立成功");
            Channel channel = channelFuture.channel();
            channel.writeAndFlush(rpcRequest);
            log.info("客户端发送请求，RpcRequest >>> {}", rpcRequest);

            // sync 堵塞获取结果
            channel.closeFuture().sync();

            AttributeKey<RpcResponse> key = AttributeKey.valueOf(Constant.RPC_RESPONSE);
            RpcResponse response = channel.attr(key).get();
            log.info("获取channel中的内容，Channel >>> {}，response >>> {}", channel.id(), response);

            return response;
        } catch (InterruptedException e) {
            log.error("NettyRpcClient error >>>>> {}", e.getMessage());
        }

        return null;
    }
}
