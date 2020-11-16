package com.shuai.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class TestServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        /*
            在netty中每个channel都有且仅有一个channelpipeline与之对应
            1）一个channel包含了一个channelpipeline，而channelpipeline中又维护了一个由channelhandlercontext
            组成的双向链表，并且那个channelhandlercontext中又关联着一个handler
            2）入站事件和出站事件在一个双向链表中，入站事件会从链表head往后传递到最后一个入站的handler
            出站事件会从链表tail往前传递到最前一个出站的handler，两种类型的handler互不干扰
         */
        //得到管道
        ChannelPipeline pipeline = socketChannel.pipeline();
        //HttpServerCodec是netty提供的处理http的编-解码器
        pipeline.addLast("MyHttpServerCodec", new HttpServerCodec());
        pipeline.addLast("MyHttpServerHandler", new TestHttpServerHandler());
    }
}
