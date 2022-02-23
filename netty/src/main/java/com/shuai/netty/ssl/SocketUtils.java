package com.shuai.netty.ssl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class SocketUtils {

    public static void sendHello(ChannelHandlerContext ctx, String from, boolean isSecure) {
        if (null != ctx) {
            send(ctx, "HELLO from SslDemo " + from + System.getProperty("line.separator"));
        }
    }

    public static void sendLineBaseText(ChannelHandlerContext ctx, String text) {
        if (null != ctx) {
            send(ctx, text + System.getProperty("line.separator"));
        }
    }

    /*
     *
     */
    private static void send(ChannelHandlerContext ctx, String log) {
        if (null != ctx && null != log) {
            ByteBuf msgBuf = Unpooled.buffer(log.length());
            msgBuf.writeBytes(log.getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(msgBuf);
        }
    }
}