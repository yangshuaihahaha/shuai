package com.shuai.单reactor单线程;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * 处理读写业务逻辑
 */
class Handler implements Runnable {
    public static final int READING = 0, WRITING = 1;
    int state;
    final SocketChannel socket;
    final SelectionKey sk;

    public Handler(SocketChannel socket, Selector selector) throws Exception {
        this.state = READING;
        this.socket = socket;
        sk = socket.register(selector, SelectionKey.OP_READ);
        sk.attach(this);
        socket.configureBlocking(false);
    }

    @Override
    public void run() {
        if (state == READING) {
            read();
        } else if (state == WRITING) {
            write();
        }
    }

    private void read() {
        process();
        //下一步处理写事件
        sk.interestOps(SelectionKey.OP_WRITE);
        this.state = WRITING;
    }

    private void write() {
        process();
        //下一步处理读事件
        sk.interestOps(SelectionKey.OP_READ);
        this.state = READING;
    }

    /**
     * task 业务处理
     */
    public void process() {
        //do something
    }
}