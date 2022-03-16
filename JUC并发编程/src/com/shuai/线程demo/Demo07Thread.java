package com.shuai.线程demo;

//程序在执行的过程中如果出现异常，锁就会被释放
//所以在异常处理过程中，有异常的话一定要多加小心处理
public class Demo07Thread {
    int count = 0;

    public synchronized void m() {
        System.out.println(Thread.currentThread().getName() + "start");
        while (true) {
            count++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "count:" + count);
            if (count == 5) {
                int a = 1 / 0;
            }
        }
    }

    public static void main(String[] args) {
        Demo07Thread demo07Thread = new Demo07Thread();
        new Thread(() -> {
            demo07Thread.m();
        }, "线程1").start();
        new Thread(() -> {
            demo07Thread.m();
        }, "线程2").start();
    }
}
