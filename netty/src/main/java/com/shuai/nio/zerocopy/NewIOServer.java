package com.shuai.nio.zerocopy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NewIOServer {

    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

            ServerSocket socket = serverSocketChannel.socket();

            socket.bind(new InetSocketAddress(7001));

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();

                int readCount = 0;
                while (-1 != readCount) {
                    readCount = socketChannel.read(buffer);
                    buffer.rewind();//倒带 position 为0，mark作废
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
