package com.shuai.yang.service.impl;

import bean.UserAddress;
import com.alibaba.dubbo.config.annotation.Service;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.shuai.yang.service.UserService;

import java.util.Arrays;
import java.util.List;

//@Service(version = "1.0.0")//dubbo暴露服务
@Service()//dubbo暴露服务
public class UserServiceImpl implements UserService {
    @Override
    @HystrixCommand
    public List<UserAddress> getUserAddressList(String userId) throws Exception {
        if (Math.random() > 0.5) {
            throw new Exception();
        }
        UserAddress address1 = new UserAddress(1, "北京市昌平区宏福科技园综合楼3层", "1", "李老师", "010-56253825", "Y");
        return Arrays.asList(address1);
    }
}
