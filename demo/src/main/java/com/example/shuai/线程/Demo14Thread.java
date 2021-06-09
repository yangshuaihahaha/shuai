package src.main.java.com.example.shuai.线程;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

//Java的concurrent包里面的CountDownLatch其实可以把它看作一个计数器，只不过这个计数器的操作是原子操作，
//同时只能有一个线程去操作这个计数器，也就是同时只能有一个线程去减这个计数器里面的值。

//你可以向CountDownLatch对象设置一个初始的数字作为计数值，任何调用这个对象上的await()方法都会阻塞，
//直到这个计数器的计数值被其他的线程减为0为止。

//CountDownLatch的一个非常典型的应用场景是：有一个任务想要往下执行，但必须要等到其他的任务执行完毕后才可以继续往下执行。假如我们这个想要继续往下执行的任务调用一个CountDownLatch对象的await()方法，
//其他的任务执行完自己的任务后调用同一个CountDownLatch对象上的countDown()方法，这个调用await()方法的任务将一直阻塞等待，直到这个CountDownLatch对象的计数值减到0为止。

//CountDownLatch的典型用法
//    1，某一个线程等待n个线程执行完毕，
//    2，实现多个线程开始执行任务的最大并行性。注意是并行性，不是并发，强调的是多个线程在某一时刻同时开始执行。
public class Demo14Thread {

    List<Integer> list = new ArrayList<>();

    CountDownLatch latch = new CountDownLatch(1);

    void add() {
        System.out.println("add启动。。。");
        for (int i = 0; i < 10; i++) {
            if (list.size() == 5) {
                latch.countDown();
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

    void size() {
        System.out.println("size启动。。。");
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("达到了第五个数");
        System.out.println("size结束。。。");
    }

    public static void main(String[] args) {
        Demo14Thread demo13Thread = new Demo14Thread();
        new Thread(demo13Thread::size, "size").start();
        new Thread(demo13Thread::add, "add").start();
    }
}
