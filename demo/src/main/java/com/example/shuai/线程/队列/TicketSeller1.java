package src.main.java.com.example.shuai.线程.队列;

import java.util.ArrayList;
import java.util.List;

public class TicketSeller1 {

    static List<String> tickets = new ArrayList<>();

    static {
        for (int i = 0; i < 1000; i++) {
            tickets.add("第" + i + "张票");
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                synchronized (TicketSeller1.class) {
                    while (!tickets.isEmpty()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("卖出了。。。" + tickets.remove(0));
                    }
                }
            }).start();
        }
    }
}
