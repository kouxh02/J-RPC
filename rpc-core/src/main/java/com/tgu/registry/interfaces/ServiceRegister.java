package com.tgu.registry.interfaces;

import java.net.InetSocketAddress;

public interface ServiceRegister {

    void register(String serviceName, InetSocketAddress serviceAddress, boolean canRetry);
}
