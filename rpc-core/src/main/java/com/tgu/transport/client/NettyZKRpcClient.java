package com.tgu.transport.client;

import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;
import com.tgu.registry.interfaces.ServiceCenter;
import com.tgu.registry.ZKServiceCenter;
import com.tgu.trace.TraceContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class NettyZKRpcClient implements RpcClient {

    private static final EventLoopGroup eventLoopGroup;
    private static final Bootstrap bootstrap;

    // 请求超时时间（秒）
    private static final int REQUEST_TIMEOUT = 10;

    private ServiceCenter serviceCenter;

    public NettyZKRpcClient() {
        this.serviceCenter = new ZKServiceCenter();
    }

    public NettyZKRpcClient(ServiceCenter serviceCenter) {
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
        Map<String, String> mdcContextMap = TraceContext.getCopy();
        try {
            InetSocketAddress address = serviceCenter.serviceDiscovery(rpcRequest.getInterfaceName());
            if (address == null) {
                log.error("服务未注册，InterfaceName >>> {}", rpcRequest.getInterfaceName());
                return RpcResponse.fail();
            }

            // 从连接池获取或创建新连接
            Channel channel = getChannel(address);
            if (!channel.isActive()) {
                log.error("获取 Channel 失败");
                return RpcResponse.fail();
            }

            // 设置 TraceContext
            channel.attr(MDCChannelHandler.TRACE_CONTEXT_KEY).set(mdcContextMap);

            // 注册请求，获取 Future
            CompletableFuture<RpcResponse> future = UnprocessedRequests.put(rpcRequest.getRequestId());

            // 发送请求
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    log.info("客户端发送请求成功: requestId={}", rpcRequest.getRequestId());
                } else {
                    log.error("客户端发送请求失败", f.cause());
                    future.completeExceptionally(f.cause());
                    UnprocessedRequests.remove(rpcRequest.getRequestId());
                }
            });

            // 等待响应（带超时）
            RpcResponse response = future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
            log.info("收到响应: requestId={}, response={}", rpcRequest.getRequestId(), response);
            return response;

        } catch (TimeoutException e) {
            log.error("请求超时: requestId={}", rpcRequest.getRequestId());
            UnprocessedRequests.remove(rpcRequest.getRequestId());
            return RpcResponse.fail();
        } catch (InterruptedException | ExecutionException e) {
            log.error("请求异常: {}", e.getMessage());
            return RpcResponse.fail();
        }
    }

    /**
     * 获取 Channel，优先从连接池获取，没有则创建新连接
     */
    private Channel getChannel(InetSocketAddress address) throws InterruptedException {
        Channel channel = ChannelProvider.get(address);
        if (channel != null && channel.isActive()) {
            return channel;
        }

        // 创建新连接
        ChannelFuture channelFuture = bootstrap.connect(address).sync();
        channel = channelFuture.channel();
        ChannelProvider.set(address, channel);
        log.info("创建新连接: {} -> {}", address, channel.id());
        return channel;
    }

    @Override
    public void close() {
        try {
            eventLoopGroup.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            log.error("NettyRpcClient close error: {}", e.getMessage());
        }
    }
}
