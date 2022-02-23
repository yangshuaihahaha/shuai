package com.shuai.nio.zerocopy;

/*
1，MappedByteBuffer 可以让文件在内存中直接修改，操作系统不需要再拷贝一次
 */

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class NIOmmap {

    public static void main(String[] args) throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile("/Users/yangshuai/project/shuai/netty/src/main/java/com/shuai/nio/1.txt", "rw");

        //获取对应的通道
        FileChannel channel = randomAccessFile.getChannel();

        /**
         * 参数1：使用的读写模式
         * 参数2：可以修改的起始位置
         * 参数3：可以映射到内存的大小, 最多修改五个字节
         * 可以直接修改的范围是0-5
         */
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);
        map.put(0, (byte) 'H');
        map.put(3, (byte)'9');

        randomAccessFile.close();

    }
}
