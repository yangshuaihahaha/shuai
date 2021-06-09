package com.shuai.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOServer {

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();

        ServerSocket serverSocket = new ServerSocket(6666);

        System.out.println("服务器启动了。。。");

        //监听端口，等待客户端连接
        while (true) {
            final Socket accept = serverSocket.accept();
            System.out.println("连接到一个客户端。。。");

            //连接后就创建一个线程与之连接
            executorService.execute(() -> {
                try {
                    handler(accept);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    public static void handler(Socket accept) throws IOException {
        byte[] bytes = new byte[1024];

        InputStream inputStream = accept.getInputStream();

        try {
            //循环客户端发送的数据
            while (true) {
                int read = inputStream.read(bytes);
                if (read != -1) {
                    System.out.println(new String(bytes, 0, read));
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
        }
    }

}
