package com.shuai.ReentrantLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLock2 {

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

    //使用tryLock进行尝试锁定，不过锁定与否，方法都将继续执行
    //可以根据返回值判断是否锁定
    //也可以指定tryLock的锁定时间
    public void m2() {
        try {
            Boolean locked = lock.tryLock(2, TimeUnit.SECONDS);
            System.out.println(locked);
            if (locked) {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ReentrantLock2 reentrantLock1 = new ReentrantLock2();
        new Thread(reentrantLock1::m2).start();
        new Thread(reentrantLock1::m1).start();
    }
}
