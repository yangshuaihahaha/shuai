package com.shuai.线程demo;

public class Demo02Thread {
    /*
    * 同步方法和非同步方法是没有影响的可以并行执行
    *
    * */
    public static synchronized void m1() {
        System.out.println("m1开始执行");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("m1执行完毕");
    }

    public static void m2() {
        System.out.println("m2开始执行");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("m2执行完毕");
    }

    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                m1();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                m2();
            }
        }).start();
    }
}
