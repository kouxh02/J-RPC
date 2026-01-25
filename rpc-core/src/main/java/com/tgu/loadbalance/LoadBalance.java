package com.tgu.loadbalance;

import java.util.List;

public interface LoadBalance {

    String balance(List<String> addressList);

}
