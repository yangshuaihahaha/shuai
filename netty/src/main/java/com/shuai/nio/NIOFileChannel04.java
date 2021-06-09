package com.shuai.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileChannel04 {

    public static void main(String[] args) throws IOException {
        FileInputStream inputStream = new FileInputStream("/Users/yangshuai/project/shuai/netty/src/main/java/com/shuai/nio/1.png");
        FileOutputStream outputStream = new FileOutputStream("/Users/yangshuai/project/shuai/netty/src/main/java/com/shuai/nio/2.png");

        FileChannel inputStreamChannel = inputStream.getChannel();
        FileChannel outputStreamChannel = outputStream.getChannel();

        outputStreamChannel.transferFrom(inputStreamChannel, 0, inputStreamChannel.size());

        inputStream.close();
        outputStream.close();
    }

}
