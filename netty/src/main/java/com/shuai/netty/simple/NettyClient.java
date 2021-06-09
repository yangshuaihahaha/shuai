package com.shuai.netty.simple;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {
    public static void main(String[] args) {
        /*
        客户端只需要一个事件循环组
        */
        EventLoopGroup eventExecutors = new NioEventLoopGroup();

        //创建服务器的启动对象，配置参数
        //注意这里是用的是Bootstrap而不是ServerBootstrap
        Bootstrap bootstrap = new Bootstrap();

        try {
            bootstrap.group(eventExecutors)//设置线程组
                    .channel(NioSocketChannel.class)//设置客户端通道的实现类
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new NettyClientHandler());
                        }
                    });//给workerGroup 的 EventLoop 对应的管道设置处理器

            System.out.println("客户端准备好了。。。");
            //启动客户端连接服务器
            ChannelFuture sync = bootstrap.connect("127.0.0.1", 6668).sync();

            //对关闭通道进行监听
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventExecutors.shutdownGracefully();
        }
    }
}
