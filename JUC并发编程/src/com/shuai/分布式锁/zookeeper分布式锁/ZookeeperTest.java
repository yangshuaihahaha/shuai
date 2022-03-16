package com.shuai.分布式锁.zookeeper分布式锁;

import java.util.concurrent.*;

/**
 * Zookeepr实现分布式锁
 */
public class ZookeeperTest {

    private static Integer inventory = 10;

    private static final int NUM = 10;

    private static LinkedBlockingDeque linkedBlockingDeque = new LinkedBlockingDeque();

    public static void main(String[] args) {
        ZookeeperLock test = new ZookeeperLock();
        try {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(inventory, inventory, 10L, TimeUnit.SECONDS, linkedBlockingDeque);
            final CountDownLatch countDownLatch = new CountDownLatch(NUM);
            long start = System.currentTimeMillis();
            for (int i = 1; i <= NUM; i++) {
                threadPoolExecutor.execute(() -> {

                    try {
                        test.lock();
                        inventory--;
                        test.unlock();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

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