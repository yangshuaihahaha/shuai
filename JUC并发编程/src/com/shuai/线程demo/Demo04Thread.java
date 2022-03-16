package com.shuai.线程demo;

//synchronized隐式的支持重入锁，调用m1()方法时，已经获取到锁的线程，能够再次调用m2()方法获取锁而不被阻塞。
public class Demo04Thread {

    public synchronized void  m1() {
        System.out.println("m1");
        m2();
    }

    public synchronized void  m2() {
        System.out.println("m2");
    }


    public static void main(String[] args) {
        Demo04Thread demo04Thread = new Demo04Thread();
        demo04Thread.m1();
    }
}
