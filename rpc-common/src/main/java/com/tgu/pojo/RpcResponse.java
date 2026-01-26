package com.tgu.pojo;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RpcResponse implements Serializable {

    // 请求ID，用于匹配请求和响应
    private String requestId;

    // 状态信息
    private int code;

    private String message;

    // 具体数据
    private Object data;

    private Class<?> dataType;

    // 构造成功信息
    public static RpcResponse sussess(Object data) {
        return RpcResponse.builder()
                .code(200)
                .data(data)
                .dataType(data.getClass())
                .build();
    }

    // 构造失败信息
    public static RpcResponse fail() {
        return RpcResponse.builder().code(500).message("服务器发生错误").build();
    }
}