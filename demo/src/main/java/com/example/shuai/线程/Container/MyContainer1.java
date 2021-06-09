package src.main.java.com.example.shuai.线程.Container;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyContainer1 {

    //condition可以通俗的理解为条件队列。
    // 当一个线程在调用了await方法以后，直到线程等待的某个条件为真的时候才会被唤醒。
    // 这种方式为线程提供了更加简单的等待/通知模式。
    // Condition必须要配合锁一起使用，因为对共享状态变量的访问发生在多线程环境下。
    // 一个Condition的实例必须与一个Lock绑定，因此Condition一般都是作为Lock的内部实现。

    private ReentrantLock lock = new ReentrantLock();
    private Condition producer = lock.newCondition();
    private Condition consumer = lock.newCondition();
    final private List<Object> list = new LinkedList<>();
    final private Integer MAX = 10;
    private Integer count = 0;

    private void put(Object a) {
        lock.lock();
        try {
            while (list.size() == MAX) {
                producer.await();
            }
            list.add(a);
            count++;
            consumer.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private synchronized Object get() {
        lock.lock();
        Object o = null;
        try {
            o = null;
            while (list.size() == 0) {
                consumer.await();
            }
            o = list.remove(0);
            count--;
            producer.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return o;
    }

    public static void main(String[] args) {
        MyContainer1 myContainer1 = new MyContainer1();

        //启动十个消费者线程
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "消费者线程启动");
                Object o = myContainer1.get();
                System.out.println("获取值: " + o);
                System.out.println(Thread.currentThread().getName() + "消费者线程结束");
            }).start();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //启动两个生产者线程
        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int finalI = i;
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "生产者线程启动");

                System.out.println("放入值: " + finalI);
                myContainer1.put(finalI);
                System.out.println(Thread.currentThread().getName() + "生产者线程结束");
            }).start();
        }
    }
}
