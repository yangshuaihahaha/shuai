package com.shuai.processfunction;

import com.shuai.SendorReading;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import redis.clients.jedis.Tuple;

import javax.security.auth.login.Configuration;

public class Process_KeyedProcessFunction {
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
        dataStream.keyBy("name")
                .process(new MyProcess())
                .print();
        executionEnvironment.execute();
    }

    //实现自定义的处理函数
    public static class MyProcess extends KeyedProcessFunction<org.apache.flink.api.java.tuple.Tuple, SendorReading, Integer> {
        ValueState<Long> tsTimer;

        public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
            tsTimer = getRuntimeContext().getState(new ValueStateDescriptor<Long>("ts-timer", Long.class));
        }

        @Override
        public void processElement(SendorReading value, Context ctx, Collector<Integer> out) throws Exception {
            out.collect(value.getName().length());

            //context
            ctx.timestamp();
            ctx.getCurrentKey();
            //ctx.output();侧输出流
            ctx.timerService().currentProcessingTime();//当前的处理事件
            ctx.timerService().currentWatermark();//当前watermark
            ctx.timerService().registerProcessingTimeTimer(10000L);//注册处理时间的定时器，处理时间到达10000毫秒的时候，触发。但是一般不会这样设置
            tsTimer.update(value.getTimestamp() + 10000L);
            ctx.timerService().registerProcessingTimeTimer(value.getTimestamp() + 10000L);//一般会这样设置
            ctx.timerService().registerEventTimeTimer(value.getTimestamp());//注册事件时间定时器
            ctx.timerService().deleteProcessingTimeTimer(tsTimer.value());//删除时间戳，根据设置的时间戳区分
            ctx.timerService().deleteEventTimeTimer(10000L);//删除时间戳，根据设置的时间戳区分
        }

        public void onTimer(long timestamp, OnTimerContext ctx, Collector<Integer> out) throws Exception {
            System.out.print(timestamp + "定时器触发");
            ctx.getCurrentKey();
            //ctx.output();//侧输出流
            ctx.timeDomain();//时间域
        }

        public void close() throws Exception {
            tsTimer.clear();
        }
    }
}
