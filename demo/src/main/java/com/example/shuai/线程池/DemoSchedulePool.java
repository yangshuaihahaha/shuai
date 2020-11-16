package src.main.java.com.example.shuai.线程池;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class DemoSchedulePool {

    public static void main(String[] args) {
        //SinglePool里面只有一个线程在执行，用来保证线程的顺序执行
        ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
        //延时多少秒执行
        service.schedule(() -> {
            System.out.println(Thread.currentThread().getName());
        }, 10, TimeUnit.SECONDS);
        

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1,
                new ThreadFactory() {
                    int index = 0;

                    public Thread newThread(Runnable r) {
                        index++;
                        return new Thread(r, "" + index);
                    }
                });
        /**
         * FixedRate表示会周期性的执行，如果执行时间长超过period的话，下一个周期会立即执行
         */
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.out.println("exectue start time =" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            System.out.println("scheule at fixed run...,threadName=" + Thread.currentThread().getName());
            try {
                Thread.sleep(10 * 1000);
            } catch (Exception e) {

            }
            System.out.println("finish time=" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }, 0, 5, TimeUnit.SECONDS);
        /**
         *  fixedDelay的话，是在上一次执行完再delay时间后执行
         */
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            System.out.println("exectue start time =" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            System.out.println("scheule at fixed run...,threadName=" + Thread.currentThread().getName());
            try {
                Thread.sleep(10 * 1000);
            } catch (Exception e) {

            }
            System.out.println("finish time=" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }, 0, 5, TimeUnit.SECONDS);
    }
}
