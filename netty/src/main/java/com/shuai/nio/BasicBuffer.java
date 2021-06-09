package com.shuai.nio;

import java.nio.IntBuffer;

public class BasicBuffer {

    public static void main(String[] args) {
        //存数据
        IntBuffer allocate = IntBuffer.allocate(5);
        for (int i = 0; i < allocate.capacity(); i++) {
            allocate.put(i * 2);
        }

        //读取存入切换
        allocate.flip();

        //读取数据
        while (allocate.hasRemaining()) {
            System.out.println(allocate.get());
        }
    }
}
