package com.example.shuai.线程.线程的单例模式;

public class MyObject2 {
    // 双重检查的模式, 线程安全, 加上volatile防止指令重排
    private static volatile MyObject2 myObject;

    public static MyObject2 getInstance() {
        if (myObject == null) {
            synchronized (MyObject2.class) {
                if (myObject == null) {
                    myObject = new MyObject2();
                }
            }
        }
        return myObject;
    }
}
