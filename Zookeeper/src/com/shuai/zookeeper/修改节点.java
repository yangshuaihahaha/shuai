package com.example.shuai.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class 修改节点 {

//    - | 参数         | 解释                                                         |
//    | ------------ | ------------------------------------------------------------ |
//    | `path`       | `znode `路径                                                 |
//    | `data`       | 数据                                                         |
//    | `acl`        | 要创建的节点的访问控制列表。`zookeeper API `提供了一个静态接口 `ZooDefs.Ids` 来获取一些基本的`acl`列表。例如，`ZooDefs.Ids.OPEN_ACL_UNSAFE`返回打开`znode`的`acl`列表 |
//    | `createMode` | 节点的类型，这是一个枚举                                     |
//    | `callBack`   | 异步回调接口                                                 |
//    | `ctx`        | 传递上下文参数

    private static ZooKeeper zookeeper;

    public static void main(String[] args) throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        zookeeper = new ZooKeeper("127.0.0.1:2181", 5000, (WatchedEvent watchedEvent) -> {
            if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                System.out.println("连接成功");
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        System.out.println(zookeeper.getSessionId());
        setData1();
        zookeeper.close();
    }

    public static void setData1() throws Exception{
        // arg1:节点的路径
        // arg2:修改的数据
        // arg3:数据的版本号 -1 代表版本号不参与更新
        Stat stat = zookeeper.setData("/hadoop","hadoop-1".getBytes(),-1);
    }

    public static void setData2() throws Exception{
        zookeeper.setData("/hadoop", "hadoop-1".getBytes(), 3 , (rc, path, ctx, stat) -> {
            // 讲道理，要判空
            System.out.println(rc + " " + path + " " + stat.getVersion() +  " " + ctx);
        }, "I am context");
    }
}
