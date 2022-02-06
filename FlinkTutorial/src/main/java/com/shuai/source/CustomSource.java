package com.shuai.source;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.util.Random;

public class CustomSource {
    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream dataStream = env.addSource(new MySource());
        dataStream.print();
        env.execute();
    }

    public static class MySource implements SourceFunction<Integer> {
        // 定义一个标志位，用来控制数据的产生
        private boolean running = true;
        public void run(SourceContext<Integer> ctx) throws Exception {
            Random random = new Random();
            while (running) {
                ctx.collect(random.nextInt());
                Thread.sleep(1000);
            }
        }
        public void cancel() {
            running = false;
        }
    }
}
