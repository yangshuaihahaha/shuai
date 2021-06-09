package com.shuai.netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        /*
        1) 创建两个线程组bossGroup, workerGroup
        2) bossGroup只是处理连接请求，真正和客户端业务处理会交给workerGroup
        3) 两个都是无限循环
        */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        /*
        Bootstrap意思是引导，一个netty应用通常由一个Bootstrap开始，主要作用是配置整个netty
        串联各个组件，netty中Bootstrap类是客户端程序的启动引导类，ServerBootstrap是服务端的启动引导类
         */
        //创建服务器的启动对象，配置参数
        ServerBootstrap bootstrap = new ServerBootstrap();


        bootstrap.group(bossGroup, workerGroup)//设置两个线程组
                .channel(NioServerSocketChannel.class)//使用NioServerSocketChannel作为服务器通道的实现
                .option(ChannelOption.SO_BACKLOG, 128)//设置线程队列得到的连接数
                .handler(null)//该 handler 对应bossGroup，childHandler对应workerGroup
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new NettyServerHandler());
                    }
                });//给workerGroup 的 EventLoop 对应的管道设置处理器

        System.out.println("服务器准备好了。。。");
        //启动服务器并绑定端口
        ChannelFuture sync = bootstrap.bind(6668).sync();

        /*
        1）Netty中所有的IO操作都是异步的，不能立刻得到消息是否被处理， 但是可以过一会等他执行完成或者直接注册一个监听
           具体的实现就是通过Future和ChannelFuture，他可以完成一个注册，当操作执行成功或失败时监听会触发监听事件
        2）常用的方法有
           Channel channel() 返回当前正在进行的IO操作通道
           ChannelFuture sync() 等待异步操作执行完成
         */
        //给cf注册监听器，监控我们关心的事件
        sync.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    System.out.println("监听端口 6668 成功");
                } else {
                    System.out.println("监听端口 6668 失败");
                }
            }
        });

        //对关闭通道进行监听
        sync.channel().closeFuture().sync();
    }
}
