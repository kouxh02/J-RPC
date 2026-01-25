package com.tgu.service;


import com.tgu.pojo.User;

// 客户端通过这个接口调用服务端的实现类
public interface UserService {
    //根据ID查询用户
    User getUserByUserId(Integer id);
    //新增一个功能
    Integer insertUserId(User user);
}