package com.shuai.线程池;

import java.util.Arrays;
import java.util.List;

//parallelStream其实就是一个并行执行的流.它通过默认的ForkJoinPool,可能提高你的多线程任务的速度.
//Stream具有平行处理能力，处理的过程会分而治之，也就是将一个大任务切分成多个小任务，这表示每个任务都是一个操作，因此像以下的程式片段：

public class DemoParallelStream2 {

    //你得到的展示顺序不一定会是1、2、3、4、5、6、7、8、9，而可能是任意的顺序，就forEach()这个操作來讲，如果平行处理时，希望最后顺序是按照原来Stream的数据顺序，那可以调用forEachOrdered()。例如：
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        numbers.parallelStream().forEach(System.out::println);

        numbers.parallelStream().forEachOrdered(System.out::println);
    }

    //stream or parallelStream？
    //1. 是否需要并行？
    //2. 任务之间是否是独立的？是否会引起任何竞态条件？
    //3. 结果是否取决于任务的调用顺序？
}
