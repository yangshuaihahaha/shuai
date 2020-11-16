package com.example.shuai.线程.线程的单例模式;

public class MyObject1 {
    // 延迟加载 再多线程模式下会有线程安全的问题
    private static MyObject1 myObject;

    public static MyObject1 getInstance() {
        if (myObject == null) {
            myObject = new MyObject1();
        }
        return myObject;
    }
}
