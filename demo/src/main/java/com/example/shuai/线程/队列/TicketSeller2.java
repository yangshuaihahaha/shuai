package src.main.java.com.example.shuai.线程.队列;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

//用队列，这是更优雅的写法，不用加锁

public class TicketSeller2 {

    static Queue<String> tickets = new ConcurrentLinkedQueue<>();

    static {
        for (int i = 0; i < 1000; i++) {
            tickets.add("第" + i + "张票");
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                while (true) {
                    String s = tickets.poll();
                    if (s == null) {
                        break;
                    } else {
                        System.out.println("卖出了。。。" + s);
                    }
                }
            }).start();
        }
    }
}
