package com.example.shuai.锁.分布式锁.Redis分布式锁;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//https://juejin.im/post/5e9473f5e51d454702460323

public class RedissonLock {
    private static Integer inventory = 1000;

    private static final int NUM = 1000;

    private static LinkedBlockingDeque linkedBlockingDeque = new LinkedBlockingDeque();

//    a：基于Redis的分布式锁。使用并发量很大、性能要求很高而可靠性问题可以通过其他方案弥补的场景
//    b：基于ZooKeeper的分布式锁。适用于高可靠（高可用），而并发量不是太高的场景


//    为什么 Lambda 表达式(匿名类) 不能访问非 final 的局部变量呢？
//    因为实例变量存在堆中，而局部变量是在栈上分配，Lambda 表达(匿名类) 会在另一个线程中执行。
//    如果在线程中要直接访问一个局部变量，可能线程执行时该局部变量已经被销毁了，
//    而 final 类型的局部变量在 Lambda 表达式(匿名类) 中其实是局部变量的一个拷贝。
//
//    当然以上情况是在 Lambda 里不在改变值的情况下，如果需要改变值，
//    或者试试还有一种办法就是将整个局部变量声明在 Lambda 里面。


    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        final RedissonClient client = Redisson.create(config);
        final RLock lock = client.getLock("lock1");

        try {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(inventory, inventory, 10L, TimeUnit.SECONDS, linkedBlockingDeque);
            final CountDownLatch countDownLatch = new CountDownLatch(NUM);
            long start = System.currentTimeMillis();
            for (int i = 1; i <= 1000; i++) {
                threadPoolExecutor.execute(() -> {
                    String id = UUID.randomUUID().toString();
                    lock.lock();
                    inventory--;
                    lock.unlock();
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
        } finally {
            client.shutdown();
        }
    }
}
