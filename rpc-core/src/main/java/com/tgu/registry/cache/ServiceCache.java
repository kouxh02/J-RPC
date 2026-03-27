package com.tgu.registry.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class ServiceCache {
    private static Map<String, List<String>> cache = new HashMap<>();

    public void addService2Cache(String serviceName, String address) {
        if (cache.containsKey(serviceName)) {
            List<String> addressList = cache.get(serviceName);
            addressList.add(address);
            log.info("name: {}, address: {}. added to cache", serviceName, address);
        } else {
            List<String> list = new ArrayList<>();
            list.add(address);
            cache.put(serviceName, list);
        }
    }

    public void replaceServiceAddress(String serviceName, String oldAddress, String newAddress) {
        if (cache.containsKey(serviceName)) {
            List<String> addressList = cache.get(serviceName);
            addressList.remove(oldAddress);
            addressList.add(newAddress);
        } else {
            log.error("{} not found", serviceName);
        }
    }


    public List<String> getServiceFromCache(String serviceName) {
        if (!cache.containsKey(serviceName)) {
            return null;
        }
        return cache.get(serviceName);
    }

    public void delete(String serviceName, String address) {
        if (!cache.containsKey(serviceName)) {
            return;
        }
        List<String> strings = cache.get(serviceName);
        strings.remove(address);
    }

}
