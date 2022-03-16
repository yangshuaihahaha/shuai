package com.example.shuai.zookeeper.分布式锁;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DistributedLock {
    private String IP = "127.0.0.1:2181";
    private final String ROOT_LOCK = "/Root_Lock";
    private final String LOCK_PREFIX = "/Lock_";
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final byte[] DATA = new byte[0];

    private ZooKeeper zookeeper;
    private String path;

    private void init() {
        // 初始化
        try {
            zookeeper = new ZooKeeper(IP, 200000, w -> {
                if (w.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("连接成功");
                }
                countDownLatch.countDown();
            });
            countDownLatch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 暴露的外部方法，主逻辑
    public void acquireLock() {
        init();
        createLock();
        attemptLock();
    }

    // 暴露的外部方法，主逻辑
    public void releaseLock() {
        try {
            zookeeper.delete(path, -1);
            System.out.println("锁释放了" + path);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    private void createLock() {
        try {
            // 创建一个目录节点
            Stat root = zookeeper.exists(ROOT_LOCK, false);
            if (root == null)
                zookeeper.create(ROOT_LOCK, DATA, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            // 目录下创建子节点
            path = zookeeper.create(ROOT_LOCK + LOCK_PREFIX, DATA, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent watchedEvent) {
            if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
                synchronized (this) {
                    this.notifyAll();
                }
            }
        }
    };

    private void attemptLock() {
        try {
            // 获取正在排队的节点，由于是zookeeper生成的临时节点，不会出错，这里不能加监视器
            // 因为添加了监视器后，任何子节点的变化都会触发监视器
            List<String> nodes = zookeeper.getChildren(ROOT_LOCK, false);
            nodes.sort(String::compareTo);
            // 获取自身节点的排名
            int ranking = nodes.indexOf(path.substring(ROOT_LOCK.length() + 1));
            // 已经是最靠前的节点了，获取锁
            if (ranking == 0) {
                return;
            } else {
                // 并不是靠前的锁，监视自身节点的前一个节点
                Stat status = zookeeper.exists(ROOT_LOCK + "/" + nodes.get(ranking - 1), watcher);
                // 有可能这这个判断的瞬间，0号完成了操作(此时我们应该判断成功自旋才对)，但是上面的status变量已经获取了值并且不为空，1号沉睡
                // 但是，请注意自行测试，虽然1号表面上沉睡了，但是实际上watcher.wait()是瞬间唤醒的
                if (status == null) {
                    attemptLock();
                } else {
                    synchronized (watcher) {
                        watcher.wait();
                    }
                    attemptLock();
                }
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}