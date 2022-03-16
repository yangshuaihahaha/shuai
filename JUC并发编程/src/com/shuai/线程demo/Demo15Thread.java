package com.shuai.线程demo;

import java.util.concurrent.CyclicBarrier;

//CyclicBarrier——回环栅栏，通过它可以实现让一组线程等待至某个状态之后再全部同时执行，
//叫做回环是因为当所有等待线程都被释放以后，CyclicBarrier可以被重用。

//CountDownLatch和CyclicBarrier的区别：
//        CountDownLatch强调的是一个线程（或多个）需要等待另外的n个线程干完某件事情之后才能继续执行。
//        CyclicBarrier强调的是n个线程，大家相互等待，只要有一个没完成，所有人都得等着。
//        CountDownLatch是不能够重用的，而CyclicBarrier是可以重用的。


public class Demo15Thread {

    public static void main(String[] args) {
        int threadNum = 5;
        CyclicBarrier barrier = new CyclicBarrier(threadNum, new Runnable() {

            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " 完成最后任务");
            }
        });

        for (int i = 0; i < threadNum; i++) {
            new TaskThread(barrier).start();
        }
    }

    static class TaskThread extends Thread {

        CyclicBarrier barrier;

        public TaskThread(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                System.out.println(getName() + " 到达栅栏 A");
                barrier.await();
                System.out.println(getName() + " 冲破栅栏 A");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
