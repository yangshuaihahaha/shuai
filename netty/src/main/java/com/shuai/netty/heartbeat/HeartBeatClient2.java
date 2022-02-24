package com.shuai.netty.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Random;

public class HeartBeatClient2 {

    int port;
    Channel channel;
    Random random;

    public HeartBeatClient2(int port) {
        this.port = port;
        random = new Random();
    }

    public static void main(String[] args) throws Exception {
        HeartBeatClient2 client = new HeartBeatClient2(8090);
        client.start();
    }

    public void start() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast(new HeartBeatClientHandler());
                        }
                    });
            connect(bootstrap, port);
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

    public void connect(Bootstrap bootstrap, int port) throws Exception {
        channel = bootstrap.connect("localhost", 8090).sync().channel();
    }

    public void sendMsg(String text) throws Exception {
        int num = random.nextInt(10);
        Thread.sleep(num * 1000);
        channel.writeAndFlush(text);
    }
}