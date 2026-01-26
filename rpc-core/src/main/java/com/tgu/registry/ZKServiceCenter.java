package com.tgu.registry;

import com.tgu.config.RpcConfig;
import com.tgu.loadbalance.LoadBalance;
import com.tgu.registry.cache.ServiceCache;
import com.tgu.registry.cache.WatchZK;
import com.tgu.loadbalance.RoundLoadBalance;
import com.tgu.loadbalance.RandomLoadBalance;
import com.tgu.registry.interfaces.ServiceCenter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZKServiceCenter implements ServiceCenter {

    private ServiceCache cache;

    private CuratorFramework client;
    
    private LoadBalance loadBalance;

    public ZKServiceCenter() {
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.builder()
                .connectString(RpcConfig.getZkConnectString())
                .sessionTimeoutMs(RpcConfig.getZkSessionTimeout())
                .retryPolicy(policy)
                .namespace(RpcConfig.getZkRootPath())
                .build();
        this.client.start();
        log.info("zookeeper 链接成功 - {}", RpcConfig.getZkConnectString());

        this.cache = new ServiceCache();
        WatchZK watchZK = new WatchZK(client, cache);
        watchZK.watch2Update(RpcConfig.getZkRootPath());
        
        // 根据配置选择负载均衡策略
        String strategy = RpcConfig.getLoadBalanceStrategy();
        this.loadBalance = switch (strategy.toLowerCase()) {
            case "random" -> new RandomLoadBalance();
            case "round" -> new RoundLoadBalance();
            default -> {
                log.warn("未知的负载均衡策略: {}, 使用默认策略: round", strategy);
                yield new RoundLoadBalance();
            }
        };
//        log.info("负载均衡策略: {}", strategy);
    }

    // 根据服务名（接口名）返回地址
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            // 获取服务名对应路径下的所有子节点，子节点通常保存服务实例的地址（ip:port）
            List<String> serviceList = cache.getServiceFromCache(serviceName);
            if (serviceList == null) {
                serviceList = client.getChildren().forPath("/" + serviceName);
            }
            String string = loadBalance.balance(serviceList);
            return parseAddress(string);
        } catch (Exception e) {
            log.error("ZKService >>> {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean checkRetry(String serviceName) {
        boolean canRetry = false;
        try {
            List<String> serviceList = client.getChildren().forPath("/" + RpcConfig.getZkRetryPath());
            for (String s : serviceList) {
                if (s.equals(serviceName)) {
//                    log.info("service: {}, can retry", serviceName);
                    canRetry = true;
                }
            }

        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
        return canRetry;
    }

    @Override
    public void close() {
        client.close();
    }


    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }


}
