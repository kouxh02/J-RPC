package com.tgu.constants;

/**
 * 常量类 - 保留部分不需要配置的常量
 * 注意：大部分配置已迁移到 application.yml 和 RpcConfig
 */
public class Constant {

    public static final String RPC_RESPONSE = "RpcResponse";

    /**
     * @deprecated 请使用 RpcConfig.getServerHost()
     */
    @Deprecated
    public static final String HOST = "127.0.0.1";

    /**
     * @deprecated 请使用 RpcConfig.getServerPort()
     */
    @Deprecated
    public static final int PORT = 9999;

    /**
     * @deprecated 请使用 RpcConfig.getRateLimitRate()
     */
    @Deprecated
    public static final int RATE_LIMIT_RATE = 200;

    /**
     * @deprecated 请使用 RpcConfig.getRateLimitCapacity()
     */
    @Deprecated
    public static final int RATE_LIMIT_CAPACITY = 3;

    /**
     * @deprecated 请使用 RpcConfig.getRetryWaitTime()
     */
    @Deprecated
    public static final int RETRY_TIME = 700;

    /**
     * @deprecated 请使用 RpcConfig.getRetryWaitTimeUnit()
     */
    @Deprecated
    public static final java.util.concurrent.TimeUnit RETRY_TIME_UNIT = java.util.concurrent.TimeUnit.MILLISECONDS;

    /**
     * @deprecated 请使用 RpcConfig.getRetryMaxAttempts()
     */
    @Deprecated
    public static final int RETRY_MAX = 3;

    /**
     * @deprecated ZK配置已迁移到 RpcConfig
     */
    @Deprecated
    public static class ZK {
        public static final String HOST = "172.31.151.142";
        public static final int PORT = 2181;
        public static final String CONNECT_STR = "%s:%d".formatted(HOST, PORT);
        public static final String ROOT_PATH = "RPC";
        public static final String RETRY = "CanRetry";
    }

}
