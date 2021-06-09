package com.example.shuai.锁.分布式锁.Redis分布式锁;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//https://juejin.im/post/5e9473f5e51d454702460323

public class RedisTest {
    private static Integer inventory = 1000;

    private static final int NUM = 1000;

    private static LinkedBlockingDeque linkedBlockingDeque = new LinkedBlockingDeque();

    public static void main(String[] args) {
        try {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(inventory, inventory, 10L, TimeUnit.SECONDS, linkedBlockingDeque);
            final CountDownLatch countDownLatch = new CountDownLatch(NUM);
            long start = System.currentTimeMillis();
            for (int i = 1; i <= NUM; i++) {
                threadPoolExecutor.execute(() -> {

                    String id = UUID.randomUUID().toString();
                    RedisLock.lock(id);
                    inventory--;
                    RedisLock.unlock(id);

                    System.out.println("线程执行：" + Thread.currentThread().getName());
                    countDownLatch.countDown();
                });
            }
            threadPoolExecutor.shutdown();
            countDownLatch.await();
            Long end = System.currentTimeMillis();
            System.out.println("执行线程：" + NUM + "   总耗时：" + (end - start) + "   总库存：" + inventory);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
