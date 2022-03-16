package com.shuai.ReentrantLock;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLock5 extends Thread {
    //指定ReentrantLock为公平锁
    ReentrantLock lock = new ReentrantLock(true);

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            lock.lock();
            System.out.println(Thread.currentThread().getName() + "获得锁");
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        ReentrantLock5 reentrantLock5 = new ReentrantLock5();

        Thread t1 = new Thread(reentrantLock5);
        Thread t2 = new Thread(reentrantLock5);

        t1.start();
        t2.start();
    }
}
