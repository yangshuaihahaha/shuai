package com.example.shuai.线程池;

import java.util.ArrayList;
import java.util.List;

//parallelStream其实就是一个并行执行的流.它通过默认的ForkJoinPool,可能提高你的多线程任务的速度.
//Stream具有平行处理能力，处理的过程会分而治之，也就是将一个大任务切分成多个小任务，这表示每个任务都是一个操作，因此像以下的程式片段：

public class DemoParallelStream1 {

    public static void main(String[] args) {
        //创建集合大小为100
        List<Integer> integers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            integers.add(i);
        }
        System.out.println("integers" + integers.size());
        //多管道遍历
        List<Integer> integerList = new ArrayList<>();
        integers.parallelStream().forEach(e -> {
            //添加list的方法
            synchronized (DemoParallelStream1.class) {
                integerList.add(e);

            }
            try {
                //休眠100ms，假装执行某些任务
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        System.out.println("integerList" + integerList.size());
    }
}
