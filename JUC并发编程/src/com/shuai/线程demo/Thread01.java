package com.shuai.线程demo;

public class Thread01 {

    static Integer count = 10;

    public static Integer getCount() {
        return count;
    }

    public static void setCount(Integer count) {
        Thread01.count = count;
    }

    //    @Override
//    public synchronized void run() {
//        count--;
//        System.out.println(Thread.currentThread().getName() + "count + " + count);
//    }
}
