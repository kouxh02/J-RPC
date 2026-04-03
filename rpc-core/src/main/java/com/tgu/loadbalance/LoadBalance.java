package com.tgu.loadbalance;

import com.tgu.spi.ExtensionLoader;
import com.tgu.spi.NamedExtension;

import java.util.List;

public interface LoadBalance extends NamedExtension {

    String balance(List<String> addressList);

    static LoadBalance getLoadBalance(String name) {
        return ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(name);
    }
}
