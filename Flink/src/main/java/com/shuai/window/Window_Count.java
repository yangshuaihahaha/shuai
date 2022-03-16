package com.shuai.window;

import com.shuai.SendorReading;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Window_Count {
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

        //开计数窗口测试
        //10个数开一个窗口，每隔两个数滑动一次
        DataStream avgTempResultStream = dataStream.keyBy("name")
                .countWindow(10, 2)
                .aggregate(new MyAvgTemp());
        avgTempResultStream.print();
    }

    public static class MyAvgTemp implements AggregateFunction<SendorReading, Tuple2<Double, Integer>, Double> {

        @Override
        public Tuple2<Double, Integer> createAccumulator() {
            return new Tuple2<Double, Integer>(0.0, 0);
        }

        @Override
        public Tuple2<Double, Integer> add(SendorReading value, Tuple2<Double, Integer> accumulator) {
            return new Tuple2<Double, Integer>(accumulator.f0 + value.getTemperature(), accumulator.f1 + 1);
        }

        @Override
        public Double getResult(Tuple2<Double, Integer> accumulator) {
            return accumulator.f0 / accumulator.f1;
        }

        @Override
        public Tuple2<Double, Integer> merge(Tuple2<Double, Integer> a, Tuple2<Double, Integer> b) {
            return new Tuple2<Double, Integer>(a.f0 + b.f0, a.f1 + b.f1);
        }
    }
}
