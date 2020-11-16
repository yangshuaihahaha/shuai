package com.shuai.nio.zerocopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class NewIOClient {

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 7001));
        String filename = "/Users/yangshuai/project/shuai/netty/src/main/java/com/shuai/nio/zerocopy/logic-master.zip";
        FileChannel fileChannel = new FileInputStream(filename).getChannel();
        long startTime = System.currentTimeMillis();
        long l = fileChannel.transferTo(0, fileChannel.size(), socketChannel);
        System.out.println("发送的总字节数 = " + l + ", 耗时 = " + (System.currentTimeMillis() - startTime));

    }
}
