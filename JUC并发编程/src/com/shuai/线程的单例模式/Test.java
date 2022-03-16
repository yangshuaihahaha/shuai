package com.shuai.线程的单例模式;

public class Test {
    public static void main(String[] args) {
        new Thread(() -> {
            System.out.println(MyObject2.getInstance().hashCode());
        }).start();
        new Thread(() -> {
            System.out.println(MyObject2.getInstance().hashCode());
        }).start();
        new Thread(() -> {
            System.out.println(MyObject2.getInstance().hashCode());
        }).start();
    }
}
