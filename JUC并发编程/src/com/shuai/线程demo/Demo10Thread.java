package com.shuai.线程demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//解决同样的问题，可以使用一些原子类--Atomic
public class Demo10Thread {

//    /*volatile*/ int count = 0;
    AtomicInteger count = new AtomicInteger(0);

    public void m() {
        System.out.println(Thread.currentThread().getName() + "start");
        for (int i = 0; i < 100000; i++) {
            count.incrementAndGet();
        }
        System.out.println(Thread.currentThread().getName() + "end");
    }

    public synchronized static void main(String[] args) {
        Demo10Thread demo09Thread = new Demo10Thread();

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(demo09Thread::m, "thread" + i));
        }

        threads.forEach((t) -> t.start());

        threads.forEach((t) -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println(demo09Thread.count);
    }
}
