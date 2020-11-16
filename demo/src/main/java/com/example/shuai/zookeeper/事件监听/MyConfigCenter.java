package com.example.shuai.zookeeper.事件监听;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class MyConfigCenter implements Watcher {

//    **配置中心案例**
//
//    工作中有这样的一个场景：数据库用户名和密码信息放在一个配置文件中，应用读取该配置文件，配置文件信息放入缓存
//    若数据库的用户名和密码改变时候，还需要重新加载媛存，比较麻烦，通过 `Zookeeper`可以轻松完成,当数据库发生变化时自动完成缓存同步
//    使用事件监听机制可以做出一个简单的配置中心
//
//    设计思路
//
//    1. 连接`zookeeper `服务器
//    2. 读取`zookeeper`中的配置信息，注册`watcher`监听器，存入本地变量
//    3. 当`zookeeper`中的配置信息发生变化时，通过`watcher`的回调方法捕获数据变化事件
//    4. 重新获取配置信息

    private static final String IP = "127.0.0.1:2181";
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ZooKeeper zooKeeper;

    private String url;
    private String username;
    private String password;

    @Override
    public void process(WatchedEvent watchedEvent) {
        Watcher.Event.KeeperState state = watchedEvent.getState();
        //当节点没有变化
        if (watchedEvent.getType() == Event.EventType.None) {
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
        } else if (watchedEvent.getType() == Event.EventType.NodeDataChanged) {
            initValue();
        }

        countDownLatch.countDown();
    }

    public MyConfigCenter() {
        initValue();
    }

    // 测试
    public static void main(String[] args) {
        MyConfigCenter myConfigCenter = new MyConfigCenter();
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("url:" + myConfigCenter.getUrl());
            System.out.println("username:" + myConfigCenter.getUsername());
            System.out.println("password:" + myConfigCenter.getPassword());
            System.out.println("#########################");
        }

    }

    private void initValue() {
        try {
            //创建连接
            zooKeeper = new ZooKeeper(IP, 5000, this);
            //阻塞连接，
            countDownLatch.await();
            //读取配置信息
            this.url = new String(zooKeeper.getData("/config/url", true, null));
            this.username = new String(zooKeeper.getData("/config/username", true, null));
            this.password = new String(zooKeeper.getData("/config/password", true, null));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}