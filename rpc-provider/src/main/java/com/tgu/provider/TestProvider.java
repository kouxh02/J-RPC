package com.tgu.provider;

import com.tgu.config.ConfigValidator;
import com.tgu.config.RpcConfig;
import com.tgu.transport.server.RpcServer;
import com.tgu.transport.server.NettyRpcServer;
import com.tgu.service.UserService;
import com.tgu.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestProvider {
    public static void main(String[] args) {
        log.info("========================================");
        log.info("        J-RPC 服务提供者启动中...");
        log.info("========================================");
        
        // 验证并打印配置
//        ConfigValidator.printAllConfig();
        if (!ConfigValidator.validateConfig()) {
            log.error("配置验证失败，服务启动中止");
            System.exit(1);
        }
        
        // 从配置文件读取服务器配置
        String host = RpcConfig.getServerHost();
        int port = RpcConfig.getServerPort();
        boolean canRetry = RpcConfig.isServiceCanRetry();
        
        log.info("启动RPC服务提供者 - 主机: {}, 端口: {}, 支持重试: {}", host, port, canRetry);
        
        UserService userService = new UserServiceImpl();
        ServiceProvider serviceProvider = new ServiceProvider(host, port);
        serviceProvider.provideServiceInterface(userService, canRetry);

        RpcServer rpcServer = new NettyRpcServer(serviceProvider);
        rpcServer.start(port);
    }
}
