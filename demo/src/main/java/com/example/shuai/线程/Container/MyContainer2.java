package com.example.shuai.线程.Container;

public class MyContainer2 extends Thread {

    @Override
    public void run() {
        for (int i = 1; i <= 100; i++) {
            System.out.println(Thread.currentThread().getName() + ":" + i);
        }
    }

    public static void main(String[] args) {
        MyContainer2 st1 = new MyContainer2();
        st1.setName("子线程1");
        st1.start();
        Thread.currentThread().setName("==========主线程");
        for (int i = 1; i <= 100; i++) {
            System.out.println(Thread.currentThread().getName() + ":" + i);
//            if (i % 10 == 0) {
//                Thread.currentThread().yield();
//            }
        }
    }
}
