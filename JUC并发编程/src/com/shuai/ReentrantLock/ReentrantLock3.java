package com.shuai.ReentrantLock;

public class ReentrantLock3 {

    public static void main(String[] args) {
        Object lock = new Object();
        Thread t1 = new Thread(() -> {
            synchronized (lock) {
                try {
                    System.out.println("t1开始执行");
                    Thread.sleep(Integer.MAX_VALUE);
                    System.out.println("t1结束");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
        Thread t2 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("t2开始执行");
                System.out.println("t2结束");
            }
        });
        t2.start();

        try {
            Thread.sleep(1000);
            t2.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
