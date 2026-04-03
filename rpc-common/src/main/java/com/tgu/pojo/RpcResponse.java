package com.tgu.pojo;

import com.tgu.enums.ResponseCode;
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
                .code(ResponseCode.SUCCESS.getCode())
                .data(data)
                .dataType(data == null ? null : data.getClass())
                .build();
    }

    // 构造失败信息
    public static RpcResponse fail() {
        return RpcResponse.builder()
                .code(ResponseCode.FAIL.getCode())
                .message("服务器发生错误")
                .build();
    }

    public static RpcResponse timeout() {
        return RpcResponse.builder()
                .code(ResponseCode.TIMEOUT.getCode())
                .message("请求超时")
                .build();
    }

    public boolean isSuccess() {
        return code == ResponseCode.SUCCESS.getCode();
    }

    public boolean isTimeout() {
        return code == ResponseCode.TIMEOUT.getCode();
    }
}
