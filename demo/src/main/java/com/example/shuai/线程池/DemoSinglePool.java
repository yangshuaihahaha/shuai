package com.example.shuai.线程池;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoSinglePool {

    public static void main(String[] args) {
        //SinglePool里面只有一个线程在执行，用来保证线程的顺序执行
        ExecutorService service = Executors.newSingleThreadExecutor();
        System.out.println(service);
        for (int i = 0; i < 6; i++) {
            service.execute(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName());
            });
        }
    }
}
