package com.tgu.transport.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Channel 连接池
 * 缓存 host:port -> Channel 的映射，实现连接复用
 */
@Slf4j
public class ChannelProvider {

    private static final Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    /**
     * 获取缓存的 Channel
     */
    public static Channel get(InetSocketAddress address) {
        String key = address.toString();
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    /**
     * 缓存 Channel
     */
    public static void set(InetSocketAddress address, Channel channel) {
        String key = address.toString();
        channelMap.put(key, channel);
        log.debug("缓存 Channel: {} -> {}", key, channel.id());
    }

    /**
     * 移除 Channel
     */
    public static void remove(InetSocketAddress address) {
        String key = address.toString();
        channelMap.remove(key);
        log.debug("移除 Channel: {}", key);
    }

    /**
     * 根据 Channel 移除
     */
    public static void remove(Channel channel) {
        channelMap.entrySet().removeIf(entry -> entry.getValue() == channel);
        log.debug("移除 Channel: {}", channel.id());
    }
}
