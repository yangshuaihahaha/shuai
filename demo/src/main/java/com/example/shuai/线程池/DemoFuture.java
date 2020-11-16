package com.example.shuai.线程池;

import java.util.concurrent.*;

public class DemoFuture {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
//        FutureTask<Integer> task = new FutureTask<>(() -> {
//            Thread.sleep(500);
//            return 1000;
//        });
//
//        new Thread(task).start();
//        System.out.println(task.get());


        ExecutorService service = Executors.newFixedThreadPool(5);
        Future<Integer> future = service.submit(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 100;
        });
        System.out.println(future.isDone());
        System.out.println(future.get());//get完就isDown了
        System.out.println(future.isDone());



    }
}
