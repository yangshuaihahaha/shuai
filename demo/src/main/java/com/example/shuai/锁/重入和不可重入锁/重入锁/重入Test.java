package com.example.shuai.锁.重入和不可重入锁.重入锁;


public class 重入Test {
    //所谓不可重入锁，即若当前线程执行某个方法已经获取了该锁，那么在方法中尝试再次获取锁时，就会获取不到被阻塞。
    public static void main(String[] args) throws InterruptedException {
        Count count = new Count();
        count.print();
    }
}
