package com.shuai.nio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileChannel01 {

    public static void main(String[] args) throws IOException {
        String str = "Hello，树先生";

        //创建一个树入流 -> channel
        FileOutputStream outputStream = new FileOutputStream("/Users/yangshuai/树先生.txt");

        //通过 fileOutputStream 获取对应的 FileChannel
        //这个 fileChannel 的真实类型是 FileChannelImpl
        FileChannel fileChannel = outputStream.getChannel();

        //创建一个Buffer缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //将str放入buffer
        byteBuffer.put(str.getBytes());

        //对bytebuffer进行flip
        byteBuffer.flip();

        //将bytebuffer数据写入到filechannel
        fileChannel.write(byteBuffer);

        outputStream.close();
    }

}
