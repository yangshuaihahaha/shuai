package com.shuai.重入和不可重入锁.重入锁;

public class Count {
    Lock lock = new Lock();
    public void print() throws InterruptedException {
        lock.lock();
        doAdd();
        lock.unlock();
    }
    public void doAdd() throws InterruptedException {
        lock.lock();
        System.out.println("doAdd");
        lock.unlock();
    }
}