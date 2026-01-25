package com.tgu.transport.client;


import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;

public interface RpcClient {

    RpcResponse sendRequest(RpcRequest rpcRequest);
}
