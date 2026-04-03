package com.tgu.pojo;

import com.tgu.enums.RequestType;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RpcRequest implements Serializable {

    // 请求ID，用于匹配请求和响应
    @Builder.Default
    private String requestId = UUID.randomUUID().toString();

    // 服务类名，客户端只知道接口，在服务端接口指向实现类
    private String interfaceName;

    // 调用的方法名
    private String methodName;

    // 参数列表
    private Object[] params;

    // 参数类型
    private Class<?>[] paramsType;

    // 链路追踪信息
    private String traceId;

    private String spanId;

    // 请求类型
    @Builder.Default
    private RequestType type = RequestType.NORMAL;

    public static RpcRequest heartBeat() {
        return RpcRequest.builder().type(RequestType.HEARTBEAT).build();
    }

}
