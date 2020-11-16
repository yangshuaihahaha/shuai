package src.main.java.com.example.shuai.线程;

import java.util.ArrayList;
import java.util.List;

//对比上一个程序，可以使用synchronize保证原子的可见性和原子性，volatile只能保证可见行
public class Demo09Thread {

    /*volatile*/ int count = 0;

    public synchronized void m() {
        System.out.println(Thread.currentThread().getName() + "start");
        for (int i = 0; i < 100000; i++) {
            count++;
        }
        System.out.println(Thread.currentThread().getName() + "end");
    }

    public synchronized static void main(String[] args) {
        Demo09Thread demo09Thread = new Demo09Thread();

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(demo09Thread::m, "thread" + i));
        }

        threads.forEach((t) -> t.start());

        threads.forEach((t) -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println(demo09Thread.count);
    }
}
