package com.shuai.线程demo;

public class Demo01Thread {

    static int count = 5;

    Object o = new Object();

    public void m1() {
        synchronized (o) {//锁定的是一个对象
            count--;
            System.out.println(Thread.currentThread().getName() + "count + " + count);
        }
    }

    public void m2() {
        synchronized (this) {//锁定当前对象
            count--;
            System.out.println(Thread.currentThread().getName() + "count + " + count);
        }
    }

    public synchronized void m3() {//简写锁定当前对象
        count--;
        System.out.println(Thread.currentThread().getName() + "count + " + count);
    }

    public static void m4() {
        //这里不能写this，因为这是一个静态方法，静态方法是不需要创建对象的
        //这里应该写Demo01Thread.class
        synchronized (Demo01Thread.class) {
            count--;
            System.out.println(Thread.currentThread().getName() + "count + " + count);
        }
    }
}
