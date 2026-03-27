package com.tgu.proxy;

import com.tgu.enums.RequestType;
import com.tgu.fault.circuitbreaker.CircuitBreaker;
import com.tgu.fault.circuitbreaker.CircuitBreakerProvider;
import com.tgu.trace.ClientTraceInterceptor;
import com.tgu.trace.TraceContext;
import com.tgu.transport.client.NettyZKRpcClient;
import com.tgu.fault.retry.GuavaRetry;
import com.tgu.transport.client.RpcClient;
import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;
import com.tgu.registry.interfaces.ServiceCenter;
import com.tgu.registry.ZKServiceCenter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@AllArgsConstructor
@Slf4j
public class ClientProxy implements InvocationHandler {

    private String host;

    private int port;

    private RpcClient rpcClient;

    private ServiceCenter serviceCenter;

    public CircuitBreakerProvider circuitBreakerProvider;


//    public ClientProxy(String host, int port, int choice) {
//        switch (choice) {
//            case 0:
//                rpcClient = new SocketRpcClient(host, port);
//            case 1:
//                rpcClient = new NettyRpcClient(host, port);
//        }
//    }

    public ClientProxy() {
        serviceCenter = new ZKServiceCenter();
        rpcClient = new NettyZKRpcClient(serviceCenter);
        circuitBreakerProvider = new CircuitBreakerProvider();
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        log.info("ClientProxy invoke 方法被调用: {}.{}", method.getDeclaringClass().getName(), method.getName());
        ClientTraceInterceptor.beforeInvoke();
        // 过滤 Object 类的方法，不作为 RPC 调用
        if (method.getDeclaringClass() == Object.class) {
            log.info("过滤：{}", method.getName());
            return method.invoke(this, args);
        }

        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .type(RequestType.NORMAL)
                .paramsType(method.getParameterTypes())
                .build();

        CircuitBreaker circuitBreaker = circuitBreakerProvider.getCircuitBreaker(request.getInterfaceName());
        if (!circuitBreaker.allowRequest()) {
            log.warn("熔断器生效，请求被过滤");
            return null;
        }
        RpcResponse response;

        if (serviceCenter.checkRetry(request.getInterfaceName())) {
            response = new GuavaRetry().sendServiceWithRetry(request, rpcClient);
        } else {
            response = rpcClient.sendRequest(request);
        }

        if (response.getCode() == 200) {
            circuitBreaker.recordSuccess();
        }
        if (response.getCode() == 500) {
            circuitBreaker.recordFailure();
        }

        ClientTraceInterceptor.afterInvoke(method.getName());
        return response.getData();
    }

    public void close() {
        rpcClient.close();
        serviceCenter.close();
    }


    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
