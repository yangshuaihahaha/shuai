package com.shuai.分布式锁.Redis分布式锁;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import java.util.Collections;

//https://juejin.im/post/5e9473f5e51d454702460323

public class RedisLock {

    private static String LOCK_KEY = "redis_lock";

    protected static long INTERNAL_LOCK_LEASE_TIME = 3;//锁过期时间

    private static long timeout = 1000;//锁超时时间

    private static SetParams params = SetParams.setParams().nx().px(INTERNAL_LOCK_LEASE_TIME);

    private static JedisPool jedisPool = new JedisPool("127.0.0.1", 6379);

//    为什么 Lambda 表达式(匿名类) 不能访问非 final 的局部变量呢？
//    因为实例变量存在堆中，而局部变量是在栈上分配，Lambda 表达(匿名类) 会在另一个线程中执行。
//    如果在线程中要直接访问一个局部变量，可能线程执行时该局部变量已经被销毁了，
//    而 final 类型的局部变量在 Lambda 表达式(匿名类) 中其实是局部变量的一个拷贝。
//
//    当然以上情况是在 Lambda 里不在改变值的情况下，如果需要改变值，
//    或者试试还有一种办法就是将整个局部变量声明在 Lambda 里面。

    public static boolean lock(String id) {
        Jedis jedis = jedisPool.getResource();
        long start = System.currentTimeMillis();
        try {
            for (; ; ) {
                //set 命令返回OK，则证明获取锁成功
                String lock = jedis.set(LOCK_KEY, id, params);
                if ("OK".equals(lock)) {
                    return true;
                }
                //否则就循环等待，在timeout时间内未获取到锁，则获取失败
                long end = System.currentTimeMillis();
                if ((start - end) >= timeout) {
                    return false;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            jedis.close();
        }
    }

    public static boolean unlock(String id) {
        Jedis jedis = jedisPool.getResource();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then" +
                " return redis.call('del',KEYS[1]) " +
                "else" +
                " return 0 " +
                "end";
        try {
            Object result = jedis.eval(script, Collections.singletonList(LOCK_KEY), Collections.singletonList(id));
            if ("1".equals(result.toString())) {
                return true;
            }
        } finally {
            jedis.close();
        }
        return false;
    }
}
