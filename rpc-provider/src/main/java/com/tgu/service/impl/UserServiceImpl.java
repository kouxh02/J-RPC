package com.tgu.service.impl;

import com.tgu.pojo.User;
import com.tgu.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.UUID;


@Slf4j
public class UserServiceImpl implements UserService {

    @Override
    public User getUserByUserId(Integer id) {
        log.info("客户端查询了{}的用户", id);
        Random random = new Random();
        User user = User.builder().userName(UUID.randomUUID().toString())
                .id(id)
                .sex(random.nextBoolean()).build();
        return user;
    }

    @Override
    public Integer insertUserId(User user) {
        log.info("插入数据成功{}", user.getUserName());
        return user.getId();
    }
}