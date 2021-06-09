package com.example.shuai.锁.分布式锁.zookeeper分布式锁;


import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * Zookeepr实现分布式锁
 */
public class ZookeeperLock {
//    1. 每个客户端往`/Locks`下创建临时有序节点`/Locks/Lock_`，创建成功后`/Locks`下面会有每个客户端对应的节点，如`/Locks/Lock_000000001`
//    2. 客户端取得/Locks下子节点，并进行排序，判断排在前面的是否为自己，如果自己的锁节点在第一位，代表获取锁成功
//    3. 如果自己的锁节点不在第一位，则监听自己前一位的锁节点。例如，自己锁节点`Lock_000000002`，那么则监听`Lock_000000001`
//    4. 当前一位锁节点`(Lock_000000001)`对应的客户端执行完成，释放了锁，将会触发监听客户端`(Lock_000000002)`的逻辑
//    5. 监听客户端重新执行第`2`步逻辑，判断自己是否获得了锁


    private String zkQurom = "localhost:2181";

    private String lockNameSpace = "/mylock";

    private String nodeString = lockNameSpace + "/test1";

    private ZooKeeper zk;

    public ZookeeperLock() {
        try {
            zk = new ZooKeeper(zkQurom, 6000, watchedEvent -> {
                System.out.println("Receive event " + watchedEvent);
                if (Watcher.Event.KeeperState.SyncConnected == watchedEvent.getState())
                    System.out.println("connection is established...");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureRootPath() throws InterruptedException {
        try {
            if (zk.exists(lockNameSpace, true) == null) {
                zk.create(lockNameSpace, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    private void watchNode(String nodeString, final Thread thread) throws InterruptedException {
        try {
            zk.exists(nodeString, watchedEvent -> {
                System.out.println("==" + watchedEvent.toString());
                if (watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted) {
                    System.out.println("Threre is a Thread released Lock==============");
                    thread.interrupt();
                }
                try {
                    zk.exists(nodeString, watchedEvent1 -> {
                        System.out.println("==" + watchedEvent1.toString());
                        if (watchedEvent1.getType() == Watcher.Event.EventType.NodeDeleted) {
                            System.out.println("Threre is a Thread released Lock==============");
                            thread.interrupt();
                        }
                        try {
                            zk.exists(nodeString, true);
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取锁
     *
     * @return
     * @throws InterruptedException
     */
    public boolean lock() throws InterruptedException {
        String path = null;
        ensureRootPath();
        watchNode(nodeString, Thread.currentThread());
        while (true) {
            try {
                path = zk.create(nodeString, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            } catch (KeeperException e) {
                System.out.println(Thread.currentThread().getName() + "  getting Lock but can not get");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    System.out.println("thread is notify");
                }
            }
            if (StringUtils.isNotBlank(path)) {
                System.out.println(Thread.currentThread().getName() + "  get Lock...");
                return true;
            }
        }
    }

    /**
     * 释放锁
     */
    public void unlock() {
        try {
            zk.delete(nodeString, -1);
            System.out.println("Thread.currentThread().getName() +  release Lock...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}