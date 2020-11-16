package com.shuai.yang.service.impl;

import bean.UserAddress;
import com.shuai.yang.service.UserService;

import java.util.List;

public class UserServiceStub implements UserService {

    //必须定义这个接口，以便接收dubbo在调用远程服务生成的服务代理类
    private UserService userLocalService;


    //这个构造函数必须要提供，dubbo框架会在消费者这一方调用这个方法
    public UserServiceStub(UserService userLocalService) {
        this.userLocalService = userLocalService;
    }


    public List<UserAddress> getUserAddressList(String userId) throws Exception {
        System.out.println("本地存根调用。。。。。。");
        if (userId != null) {
            return userLocalService.getUserAddressList(userId);
        }
        return null;
    }
}
