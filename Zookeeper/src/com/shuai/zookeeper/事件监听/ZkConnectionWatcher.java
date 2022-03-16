package com.shuai.zookeeper.事件监听;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// 客户端**首先将 `Watcher`注册到服务端**，同时将 `Watcher`对象**保存到客户端的`watch`管理器中**。
// 当`Zookeeper`服务端监听的数据状态发生变化时，服务端会**主动通知客户端**，
// 接着客户端的 `Watch`管理器会**触发相关 `Watcher`**来回调相应处理逻辑，从而完成整体的数据 `发布/订阅`流程

//| 一次性         | `watcher`是**一次性**的，一旦被触发就会移除，再次使用时需要重新注册 |
//| 客户端顺序回调  | `watcher`回调是**顺序串行**执行的，只有回调后客户端才能看到最新的数据状态。一个`watcher`回调逻辑不应该太多，以免影响别的`watcher`执行 |
//| 轻量级         | `WatchEvent`是最小的通信单位，结构上只包含**通知状态、事件类型和节点路径**，并不会告诉数据节点变化前后的具体内容 |
//| 时效性         | `watcher`只有在当前`session`彻底失效时才会无效，若在`session`有效期内快速重连成功，则`watcher`依然存在，仍可接收到通知； |


//Watcher通知状态(KeeperState)
//| `SyncConnected` | 客户端与服务器正常连接时 |
//| `Disconnected`  | 客户端与服务器断开连接时 |
//| `Expired`       | 会话`session`失效时      |
//| `AuthFailed`    | 身份认证失败时           |

//Watcher事件类型(EventType)
//| `None`                | 无                                                          |
//| `NodeCreated`         | `Watcher`监听的数据节点被创建时                             |
//| `NodeDeleted`         | `Watcher`监听的数据节点被删除时                             |
//| `NodeDataChanged`     | `Watcher`监听的数据节点内容发生更改时(无论数据是否真的变化) |
//| `NodeChildrenChanged` | `Watcher`监听的数据节点的子节点列表发生变更时               |

public class ZkConnectionWatcher {
    // 客户端与服务器端的连接状态

    private static final String IP = "127.0.0.1:2181";
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        // 5000为会话超时时间
        ZooKeeper zooKeeper = new ZooKeeper(IP, 5000, watchedEvent -> {
            Watcher.Event.KeeperState state = watchedEvent.getState();
            if (state == Watcher.Event.KeeperState.SyncConnected) {
                // 正常
                System.out.println("正常连接");
            } else if (state == Watcher.Event.KeeperState.Disconnected) {
                // 可以用Windows断开虚拟机网卡的方式模拟
                // 当会话断开会出现，断开连接不代表不能重连，在会话超时时间内重连可以恢复正常
                System.out.println("断开连接");
            } else if (state == Watcher.Event.KeeperState.Expired) {
                // 没有在会话超时时间内重新连接，而是当会话超时被移除的时候重连会走进这里
                System.out.println("连接过期");
            } else if (state == Watcher.Event.KeeperState.AuthFailed) {
                // 在操作的时候权限不够会出现
                System.out.println("授权失败");
            }
            countDownLatch.countDown();
        });


        countDownLatch.await();
        // 模拟授权失败
        byte[] data = zooKeeper.getData("/hadoop", false, null);
        System.out.println(new String(data));
        TimeUnit.SECONDS.sleep(50);
    }
}