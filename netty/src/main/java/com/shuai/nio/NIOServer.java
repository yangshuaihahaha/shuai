package com.shuai.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


/*具体的思路
1）当客户端进行连接时，会通过ServerSocketChannel得到SocketChannel
2）将socketChannel注册到Selector上register(Selector sel, int ops),一个selector上可以注册多个SocketChannel
3）注册后返回SelectionKey，会和Selector关联
4）Selector进行监听select方法，返回有事件发生的通道个数
5）进一步得到各个SelectionKey(有事件发生)
6）再通过SelectionKey反向获取SocketChannel
7）可以通过得到的channel，完成业务处理
8）代码撑腰。。。。。。
 */

public class NIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));
        //得到Selector对象
        Selector selector = Selector.open();

        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("keys的大小。。。。。。" + selector.keys().size());
        while (true) {
            if (selector.select(1000) == 0) {
                System.out.println("服务器等待了1秒，无连接");
                continue;
            }

            //如果返回的大于0，就获取到相关的 selectionKey集合
            //selector.selectedKeys() 返回的是关注事件的集合
            //通过selectionKeys反向获取通道
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            System.out.println("selectionKeys大小。。。。。。" + selectionKeys.size());
            System.out.println("keys大小。。。。。。" + selector.keys().size());

            while (keyIterator.hasNext()) {
                SelectionKey selectionKey = keyIterator.next();

                //根据key对相应的通道进行处理

                //如果是OP_ACCEPT，有新的客户端连接
                if (selectionKey.isAcceptable()) {
                    //该客户端生成一个SocketChannel
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    //将SocketChannel注册到selector，关注事件为OP_READ, 同时给socketChannel
                    //关联一个Buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }

                //如果是OP_READ
                if (selectionKey.isReadable()) {
                    //通过key，反向获取到channel
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    channel.configureBlocking(false);

                    //获取到该channel关联的buffer
                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                    channel.read(buffer);
                    System.out.println("from 客户端" + new String(buffer.array()));
                }

                //手动从集合中移除 当前的key防止重复操作
                keyIterator.remove();
            }
        }
    }
}
