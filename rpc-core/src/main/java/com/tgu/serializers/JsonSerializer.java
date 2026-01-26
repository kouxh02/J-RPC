package com.tgu.serializers;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.tgu.enums.RequestType;
import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        // log.info(" serialize >>> {}", obj);
        return JSON.toJSONBytes(obj);
    }

    @Override
    public Object deserializer(byte[] bytes, int messageType) {
        // log.info(" deserializer >>> {}", new String(bytes));
        Object obj = null;

        switch (messageType) {
            case 0:
                // log.info(">>>>> 服务端视角，解析RpcRequest");
                // 将字节数组转化为 RpcRequest 对象
                RpcRequest rpcRequest = JSON.parseObject(bytes, RpcRequest.class,
                        JSONReader.Feature.SupportClassForName);

                if (rpcRequest.getType() == RequestType.HEARTBEAT) {
                    obj = rpcRequest;
                    break;
                }
                 log.info("初步parse字节数组 >>> {}", rpcRequest);

                // 存储解析后的请求参数
                Object[] objects = new Object[rpcRequest.getParamsType().length];

                for (int i = 0; i < objects.length; i++) {
                    Class<?> paramsType = rpcRequest.getParamsType()[i];
                    if (!paramsType.isAssignableFrom(rpcRequest.getParams()[i].getClass())) {
                        objects[i] = JSON.to(rpcRequest.getParamsType()[i], rpcRequest.getParams()[i]);
                    } else {
                        objects[i] = rpcRequest.getParams()[i];
                    }
                }
                // log.info("解析后的请求参数 >>> {}", objects);
                rpcRequest.setParams(objects);
                obj = rpcRequest;
                break;
            case 1:
                // log.info(">>>>> 客户端视角，解析RpcResponse");
                RpcResponse response = JSON.parseObject(bytes, RpcResponse.class,
                        JSONReader.Feature.SupportClassForName);
                // log.info(">>>>> response: {}", response);

                // 修复：当 data 为 null 时（如限流响应），不进行类型转换
                if (response.getData() != null) {
                    Class<?> dataType = response.getDataType();
                    if (dataType != null && !dataType.isAssignableFrom(response.getData().getClass())) {
                        response.setData(JSON.to(response.getDataType(), response.getData()));
                    }
                }
                obj = response;
                break;
            default:
                log.error("暂不支持此类型");
        }
        return obj;
    }

    @Override
    public int getType() {
        return 1;
    }
}
