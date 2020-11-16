package com.shuai.yang.service.impl;

import bean.UserAddress;
import com.alibaba.dubbo.config.annotation.Service;
import com.shuai.yang.service.UserService;

import java.util.Arrays;
import java.util.List;

@Service(version = "2.0.0")//dubbo暴露服务
public class UserServiceImpl2 implements UserService {
    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress address2 = new UserAddress(2, "深圳市宝安区西部硅谷大厦B座3层（深圳分校）", "1", "王老师", "010-56253825", "N");
        return Arrays.asList(address2);
    }
}
