package com.example.shuai.线程池;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//基本上所有的线程池都是基于ThreadPoolExecutor来实现的

//(1)corePoolSize： 线程池维护线程的最少数量 （core : 核心）
//(2)maximumPoolSize： 线程池维护线程的最大数量
//(3)keepAliveTime： 线程池维护线程所允许的空闲时间
//(4)unit： 线程池维护线程所允许的空闲时间的单位
//(5)workQueue： 线程池所使用的缓冲队列
//(6)handler： 线程池对拒绝任务的处理策略

public class DemoThreadPoolExcutor {
    private static int produceTaskSleepTime = 5;
    private static int consumeTaskSleepTime = 5000;
    private static int produceTaskMaxNumber = 20; //定义最大添加10个线程到线程池中

    public static void main(String[] args) {
        //构造一个线程池
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 4, 3,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(3),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        for (int i = 1; i <= produceTaskMaxNumber; i++) {
            try {
                //一个任务，并将其加入到线程池
                String work = "work@ " + i;
                System.out.println("put ：" + work);
                threadPool.execute(new ThreadPoolTask(work));
                //便于观察，等待一段时间
                Thread.sleep(produceTaskSleepTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 线程池执行的任务
     *
     * @author zhu
     */
    public static class ThreadPoolTask implements Runnable, Serializable {
        private static final long serialVersionUID = 0;
        //保存任务所需要的数据
        private Object threadPoolTaskData;

        ThreadPoolTask(Object works) {
            this.threadPoolTaskData = works;
        }

        public void run() {
            //处理一个任务，这里的处理方式太简单了，仅仅是一个打印语句
            System.out.println("start------" + threadPoolTaskData);
            try {
                //便于观察，等待一段时间
                Thread.sleep(consumeTaskSleepTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            threadPoolTaskData = null;
        }

        public Object getTask() {
            return this.threadPoolTaskData;
        }
    }
}