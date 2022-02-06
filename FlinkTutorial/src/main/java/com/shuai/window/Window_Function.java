package com.shuai.window;

import com.shuai.SendorReading;
import org.apache.commons.collections.IteratorUtils;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/*
窗口函数（window function）
    window function定义了要对窗口中收集的数据做的计算操作可以分为两类：
        增量聚合函数（incremental aggregation functions）：
            每条数据进来就计算，保持一个简单的状态
            ReduceFunction, AggregateFunction
        全窗口函数（full window functions）
            先把窗口所有数据收集起来，等到计算的时候会遍历所有数据
            ProcessWindowFunction, WindowFunction
 */

public class Window_Function {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        executionEnvironment.setParallelism(1);
        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        DataStream<String> inputStream = executionEnvironment.readTextFile(inputPath);

        DataStream<SendorReading> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
        });

        //增量聚合函数
        DataStream<Integer> resultStream = dataStream.keyBy("name")
                .timeWindow(Time.seconds(15))
                .aggregate(new AggregateFunction<SendorReading, Integer, Integer>() {
                    @Override
                    public Integer createAccumulator() {
                        return 0;
                    }

                    @Override
                    public Integer add(SendorReading value, Integer accumulator) {
                        return accumulator + 1;
                    }

                    @Override
                    public Integer getResult(Integer accumulator) {
                        return accumulator;
                    }

                    @Override
                    public Integer merge(Integer a, Integer b) {
                        return a + b;
                    }
                });

        //全窗口函数
        DataStream<Integer> resultStream2 = dataStream.keyBy("name")
                .timeWindow(Time.seconds(15))
                .apply(new WindowFunction<SendorReading, Integer, Tuple, TimeWindow>() {
                    @Override
                    public void apply(Tuple tuple, TimeWindow window, Iterable<SendorReading> input, Collector<Integer> out) throws Exception {
                        Integer count = IteratorUtils.toList(input.iterator()).size();
                        out.collect(count);
                    }
                });
    }
}
