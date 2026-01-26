package com.tgu.loadbalance;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RoundLoadBalance implements LoadBalance{


    // 1. 定义原子计数器，用于记录轮询位置
    private final AtomicInteger position = new AtomicInteger(0);


    @Override
    public String balance(List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            log.error("服务地址列表为空，无法执行负载均衡");
            throw new IllegalArgumentException("Address list cannot be null or empty");
        }
        int current = position.getAndIncrement();
        int index = (current & Integer.MAX_VALUE) % addressList.size();
        String selectedServer = addressList.get(index);
//        log.info("负载均衡选择了服务器: {}, 索引: {}", selectedServer, index);
        return selectedServer;
    }
}
