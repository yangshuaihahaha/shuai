package com.example.shuai.zookeeper.事件监听;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class EventTypeTest {

    // watcher检查节点

    private static final String IP = "127.0.0.1:2181";
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ZooKeeper zooKeeper;

    // 测试
    public static void main(String[] args) throws Exception {
        zooKeeper = new ZooKeeper(IP, 5000, watchedEvent -> {
            countDownLatch.countDown();
            System.out.println("zk的监听器" + watchedEvent.getType());
        });
        countDownLatch.await();
        exists1();
        TimeUnit.SECONDS.sleep(500);
    }

    // 采用zookeeper连接创建时的监听器
    public static void exists1() throws Exception {
        zooKeeper.exists("/watcher1", true);
    }

    // 自定义监听器
    public static void exists2() throws Exception {
        zooKeeper.exists("/node", w -> System.out.println("自定义" + w.getType()));
    }

    // 演示使用多次的监听器
    public static void exists3() throws Exception {
        zooKeeper.exists("/node", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                try {
                    System.out.println("自定义的" + watchedEvent.getType());
                } finally {
                    try {
                        zooKeeper.exists("/node", this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}