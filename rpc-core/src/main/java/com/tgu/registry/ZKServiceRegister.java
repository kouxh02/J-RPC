package com.tgu.registry;

import com.tgu.config.RpcConfig;
import com.tgu.registry.interfaces.ServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.net.InetSocketAddress;


@Slf4j
public class ZKServiceRegister implements ServiceRegister {

    private CuratorFramework client;

    public ZKServiceRegister() {
        // 重试策略：指数退避，基数1s，重试3次
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.builder()
                .connectString(RpcConfig.getZkConnectString())
                .sessionTimeoutMs(RpcConfig.getZkSessionTimeout())
                .retryPolicy(policy)
                .namespace(RpcConfig.getZkRootPath())
                .build();
        this.client.start();
        log.info("zookeeper 连接成功 - {}", RpcConfig.getZkConnectString());
    }

    @Override
    public void register(String serviceName, InetSocketAddress serviceAddress, boolean canRetry) {
        try {
            // 1. 确保服务名（父节点）存在
            // 注意：服务名是持久的，服务都挂了也不应该删掉目录
            if (client.checkExists().forPath("/" + serviceName) == null) {
                try {
                    client.create()
                            .creatingParentsIfNeeded()
                            .withMode(CreateMode.PERSISTENT)
                            .forPath("/" + serviceName);
                } catch (KeeperException.NodeExistsException e) {
                    log.error(e.getMessage());
                }
            }

            // 2. 注册具体实例（子节点）
            // 格式：/RPC/com.tgu.service.UserService/ip:port
            String path = "/" + serviceName + "/" + getServiceAddress(serviceAddress);

            // 这样服务挂掉断开连接后，ZK会自动删除此节点
            client.create()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);

            log.info("服务已注册: {}", path);

            if (canRetry) {
                path = "/" + RpcConfig.getZkRetryPath() + "/" + serviceName;
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path);
            }

        } catch (Exception e) {
            log.error("zk register error >>> {}", e.getMessage());
        }
    }

    // 【建议修改】：优先使用 IP 而不是 HostName
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getAddress().getHostAddress() +
                ":" +
                serverAddress.getPort();
    }
}
