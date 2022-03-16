package com.shuai.ReentrantLock;

//Re

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLock1 {

    ReentrantLock lock = new ReentrantLock();

    public void m1() {
        lock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                TimeUnit.SECONDS.sleep(i);
                System.out.println(i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

    public void m2() {
        lock.lock();
        System.out.println("m2方法执行");
        lock.unlock();
    }

    public static void main(String[] args) {
        ReentrantLock1 reentrantLock1 = new ReentrantLock1();
        new Thread(reentrantLock1::m2).start();
        new Thread(reentrantLock1::m1).start();
    }
}
