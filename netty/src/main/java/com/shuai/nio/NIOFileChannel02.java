package com.shuai.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileChannel02 {

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/yangshuai/树先生.txt");
        FileInputStream inputStream = new FileInputStream(file);

        //通过 fileOutputStream 获取对应的 FileChannel
        //这个 fileChannel 的真实类型是 FileChannelImpl
        FileChannel fileChannel = inputStream.getChannel();

        //创建一个Buffer缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());

        //通过通道将数据写入buffer
        fileChannel.read(byteBuffer);

        System.out.println(new String(byteBuffer.array()));
    }

}
