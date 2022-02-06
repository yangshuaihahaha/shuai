package com.shuai.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;


public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /*
    ChannelHandlerContext ctx上下文对象，含有管道pipeline，通道channel
     */
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("server ctx" + ctx);
//        ByteBuf buf = (ByteBuf) msg;
//        System.out.println("客户端发送的消息是：" + buf.toString(CharsetUtil.UTF_8));
//        System.out.println("客户端的地址是：" + ctx.channel().remoteAddress());
//    }

    /*
    ChannelHandlerContext ctx上下文对象，含有管道pipeline，通道channel
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //如果是这样的话就耗费太多时间才能返回
        Thread.sleep(5 * 1000);
        ctx.writeAndFlush(Unpooled.copiedBuffer("channelRead。。。", CharsetUtil.UTF_8));

        //任务队列三种典型的使用场景
        //场景1: 用户自定义普通的任务
        ctx.channel().eventLoop().execute(() -> {
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ctx.writeAndFlush(Unpooled.copiedBuffer("channelRead1。。。", CharsetUtil.UTF_8));
        });

        //注意这里会休眠十五秒才会返回数据
        ctx.channel().eventLoop().execute(() -> {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ctx.writeAndFlush(Unpooled.copiedBuffer("channelRead1。。。", CharsetUtil.UTF_8));
        });

        //场景2：用户自定义定时任务（该任务放在scheduleTaskQueue）
        ctx.channel().eventLoop().schedule(() -> {
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ctx.writeAndFlush(Unpooled.copiedBuffer("channelRead1。。。", CharsetUtil.UTF_8));
        }, 5, TimeUnit.SECONDS);

        //场景3：非当前Reactor线程调用Channel
        //例如推送系统的业务线程里面，根据用户标示，找到对应的Channel，然后调用Write类方法向该用户推送消息
        //最终Write会提交到任务队列中被异步消费
    }

    //数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端 channelReadComplete", CharsetUtil.UTF_8));
    }

    //处理异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
