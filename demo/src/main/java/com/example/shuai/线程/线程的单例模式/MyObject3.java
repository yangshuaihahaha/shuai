package com.example.shuai.线程.线程的单例模式;

public class MyObject3 {
    // 静态内部类单例模式是一种比较优秀的实现方式
    private static MyObject3 myObject;

    private static class MyObject3Handler {
        public static MyObject3 myObject = new MyObject3();
    }

    public static MyObject3 getInstance() {
        return MyObject3Handler.myObject;
    }
}
