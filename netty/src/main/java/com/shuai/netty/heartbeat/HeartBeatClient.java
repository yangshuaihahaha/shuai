package com.shuai.netty.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HeartBeatClient {
    private String host;
    private int port;
    private Bootstrap bootstrap;
    private Channel channel;
    private Random random;
    /** 重连策略 */
    private RetryPolicy retryPolicy;

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public HeartBeatClient(String host, int port, RetryPolicy retryPolicy) {
        this.host = host;
        this.port = port;
        this.random = new Random();
        this.retryPolicy = retryPolicy;
        init();
    }

    private void init() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new StringDecoder());
                        pipeline.addLast("encoder", new StringEncoder());
                        //每隔4s检查一下是否有写事件，如果没有就触发 HeartBeatClientHandler 中的 userEventTriggered向服务端发送心跳
                        pipeline.addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new HeartBeatClientHandler());
                        pipeline.addLast(new ReconnectHandler(HeartBeatClient.this));
                    }
                });
        bootstrap = b;
    }

    public static void main(String[] args) throws Exception {
        HeartBeatClient client = new HeartBeatClient("localhost", 8090, new ExponentialBackOffRetry(1000, Integer.MAX_VALUE, 60 * 1000));
        client.start();
    }

    public void start() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            connect();
            String text = "I am alive";
            while (channel.isActive()) {
                sendMsg(text);
            }
        } catch (Exception e) {
            // do something
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public void connect() throws Exception {
        channel = bootstrap.connect(host, 8090).sync().channel();
    }

    public void sendMsg(String text) throws Exception {
        int num = random.nextInt(10);
        Thread.sleep(num * 1000);
        channel.writeAndFlush(text);
    }
}