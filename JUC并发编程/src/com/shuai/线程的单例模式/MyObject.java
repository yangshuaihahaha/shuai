package com.shuai.线程的单例模式;

public class MyObject {
    // 立即加载方式==恶汉模式
    private static MyObject myObject = new MyObject();

    public static MyObject getInstance() {
        return myObject;
    }
}
