package com.shuai.线程demo;

//不要以字符串常量作为锁定对象 在下面的例子中，m1和m2其实锁定的是同一个对象
//这种情况还会发生比较诡异的现象，比如你用到了一个类库，在该类库中代码锁定了字符串“Hello”， 但是你读不到源码，所以你在自己的代码中也锁定了"Hello",
//这时候就有可能发生非常诡异的死锁阻塞， 因为你的程序和你用到的类库不经意间使用了同一把锁

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//不同包不同类中的值相同的字符串常量引用的是同一个字符串对象。
public class Demo13Thread {

    List<Integer> list = new ArrayList<>();

    Object lock = new Object();

    void add() {
        synchronized (lock) {
            System.out.println("add启动。。。");
            for (int i = 0; i < 10; i++) {
                if (list.size() == 5) {
                    //进入等待，并释放锁
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //通知线程size执行
//                    lock.notify();
                }
                System.out.println("Add --- " + i);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                list.add(i);
            }
            System.out.println("add结束。。。");
        }
    }

    void size() {
        synchronized (lock) {
            //通知add继续执行
            lock.notify();
            System.out.println("达到了五个数。。。");
        }
    }

    public static void main(String[] args) {
        Demo13Thread demo13Thread = new Demo13Thread();
        new Thread(demo13Thread::add, "add").start();
        new Thread(demo13Thread::size, "size").start();
    }
}
