package com.example.shuai.线程池;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoCachePool {

    public static void main(String[] args) throws InterruptedException {
        //CachedPool是没有限制的，一直增加到cup所能承受的最大线程数为止，如果其中的线程等待时间数超过60秒，就会自动关闭
        ExecutorService service = Executors.newCachedThreadPool();
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
        System.out.println(service);

        Thread.sleep(80000);

        System.out.println(service);

    }
}
