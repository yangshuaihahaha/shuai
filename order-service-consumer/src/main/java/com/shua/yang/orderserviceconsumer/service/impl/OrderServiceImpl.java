package com.shua.yang.orderserviceconsumer.service.impl;

import bean.UserAddress;
import com.alibaba.dubbo.config.annotation.Reference;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.shuai.yang.service.OrderService;
import com.shuai.yang.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    /*
    dubbo配置的默认关系
    方法级 > 接口级 > 全剧配置级
    如果级别一样那么消费之优先提供方次之

    retries 重试次数
    1）如果失败的话，重试3次
    2）如果有多个服务提供方的话，会重试其他服务器的方法
    3）幂等性的方法（可以设置重试次数）【查询，删除，修改】，非幂等性（不能设置重试次数）【新增】

    zookeeper宕机与dubbo直连
    健壮性
    1）监控中心宕机后并不影响使用只是丢失部分数据
    2）数据库宕机后，注册中心仍然可以提供查询服务，但是不能进行注册服务
    3）注册中心对等的集群，任意一台宕机后自动切换到另一台
    4）注册中心全部宕机后，服务的提供者和消费中仍然可以通过本地缓存实现通讯
    5）服务提供者无状态，任意一台宕机后并不影响使用
    6）服务提供者全部宕机后，服务消费者将无法使用，并且无限次重联等待服务提供者恢复


    @Reference(timeout = 1000)//超时时间
    @Reference(retries = 3)//重试次数
    @Reference(version = "2.0.0")//版本号机制
    @Reference(stub = "com.shuai.yang.service.impl.UserServiceStub")//本地存根
    @Reference(url = "127.0.0.1:20880")//dubbo直连


    集群的容错
    Failfast Cluster
    快速失败，只发起一次调用，失败立即报错，适用于幂等性操作，比如新增记录
    Failsafe Cluster
    失败安全，出现异常时，直接忽略，通常用于写入审计日志的操作
    Failback Cluster
    失败自动恢复，后台记录失败请求，定时重发，通常用于消息通知操作
    Forking Cluster
    并行调用多个服务器，只要一个成功就返回，通常用于实时性较高的读操作，但是需要浪费更多
    服务资源，可通过 forks="2" 来设置最大并行数
    Broadcast Cluster
    广播所有的提供者，逐个调用，任意一台报错则报错，适用于通知所有提供者更新缓存或者本地资源信息
    配置
    @Reference(cluster = "failsafe")
    @Service(cluster = "failsafe")


    一次完整的RPC调用流程（同步调用，异步另说）如下：
    1）服务消费方（client）调用以本地调用方式调用服务；
    2）client stub接收到调用后负责将方法、参数等组装成能够进行网络传输的消息体；
    3）client stub找到服务地址，并将消息发送到服务端；
    4）server stub收到消息后进行解码；
    5）server stub根据解码结果调用本地的服务；
    6）本地服务执行并将结果返回给server stub；
    7）server stub将返回结果打包成消息并发送至消费方；
    8）client stub接收到消息，并进行解码；
    9）服务消费方得到最终结果。

    */
    @Reference
    UserService userService;

    @Override
    @HystrixCommand(fallbackMethod = "hello")
    public List<UserAddress> initOrder(String userId) throws Exception {
        List<UserAddress> addressList = userService.getUserAddressList(userId);
        return addressList;
    }

    public List<UserAddress> hello(String userId) {
        List<UserAddress> addressList = Arrays.asList(new UserAddress(2, "测试", "测试", "测试", "测试", "测试"));
        return addressList;
    }
}
