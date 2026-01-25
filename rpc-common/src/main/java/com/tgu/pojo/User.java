package com.tgu.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    //实现了 Serializable 接口，表示该类可以序列化。
    // 客户端和服务端共有的
    private Integer id;
    private String userName;
    private Boolean sex;
}