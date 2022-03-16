package com.shuai.线程池;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoFixedPool {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(5);
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

        service.shutdown();//这里关闭后，不会杀死所有线程，而是等待所有线程执行完毕
        //com.shuai.orderserviceconsumer.service.shutdownNow(); //立马结束
        System.out.println(service.isTerminated());
        System.out.println(service.isShutdown());
        System.out.println(service);

        Thread.sleep(5000);
        System.out.println(service.isTerminated());
        System.out.println(service.isShutdown());
        System.out.println(service);

    }
}
