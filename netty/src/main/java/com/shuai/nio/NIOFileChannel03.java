package com.shuai.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileChannel03 {

    public static void main(String[] args) throws IOException {
        FileInputStream inputStream = new FileInputStream("/Users/yangshuai/project/shuai/netty/src/main/java/com/shuai/nio/1.txt");
        FileChannel inputStreamChannel = inputStream.getChannel();

        FileOutputStream outputStream = new FileOutputStream("/Users/yangshuai/project/shuai/netty/src/main/java/com/shuai/nio/2.txt");
        FileChannel outputStreamChannel = outputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(512);

        //循环读取
        while (true) {
            //这里有一个重要的操作
            byteBuffer.clear();
            int read = inputStreamChannel.read(byteBuffer);
            //表示读取完毕
            if (read == -1) {
                break;
            }
            //将buffer中的数据写入到outputStreamChannel
            byteBuffer.flip();
            outputStreamChannel.write(byteBuffer);
        }

        inputStream.close();
        outputStream.close();
    }

}
