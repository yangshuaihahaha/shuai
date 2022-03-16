package com.shuai.ReentrantLock;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLock4 {



    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        Thread t1 = new Thread(() -> {
            try {
                lock.lock();
                System.out.println("t1开始执行");
                Thread.sleep(10000000);
                System.out.println("t1结束");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });
        t1.start();

        Thread t2 = new Thread(() -> {
            try {
                lock.lockInterruptibly();
                System.out.println("t2开始执行");
                System.out.println("t2结束");
                lock.unlock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t2.start();

        //线程在sleep或wait,join， 此时如果别的进程调用此进程的 interrupt（）方法，此线程会被唤醒并被要求处理InterruptedException；
        //此线程在运行中， 则不会收到提醒。但是 此线程的 “打扰标志”会被设置， 可以通过isInterrupted()查看并 作出处理。
        try {
            Thread.sleep(1000);
            t2.interrupt();//打断线程2的等待
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
