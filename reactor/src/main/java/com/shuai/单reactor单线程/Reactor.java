package com.shuai.单reactor单线程;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

class Reactor implements Runnable {
    final Selector selector;
    final ServerSocketChannel serverSocket;

    private Reactor() throws Exception {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 8888);
        serverSocket.socket().bind(address);
        serverSocket.configureBlocking(false);

        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        // attach Acceptor 处理新连接
        sk.attach(new Acceptor());
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set selected = selector.selectedKeys();
                Iterator it = selected.iterator();
                while (it.hasNext()) {
                    it.remove();
                    //分发事件处理
                    dispatch((SelectionKey) (it.next()));
                }
            }
        } catch (IOException ex) {
            //do something
        }
    }

    void dispatch(SelectionKey k) {
        // 若是连接事件获取是acceptor
        // 若是IO读写事件获取是handler
        Runnable runnable = (Runnable) (k.attachment());
        if (runnable != null) {
            runnable.run();
        }
    }

    class Acceptor implements Runnable { // inner
        @Override
        public void run() {
            try {
                System.out.println("-->ready for accept!");
                SocketChannel c = serverSocket.accept();
                if (c != null) {
                    //调用Handler来处理channel
                    new Handler(c, selector);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

