package com.shuai.transform;

import com.shuai.SendorReading;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class Transform_Reduce {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        executionEnvironment.setParallelism(1);

        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        DataStream<String> inputStream = executionEnvironment.readTextFile(inputPath);

        //使用lamda表达式
        DataStream<SendorReading> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
        });

        //分组
        KeyedStream<SendorReading, Tuple> keyedStream = dataStream.keyBy("name");

        //reduce聚合，取最大值温度值，以及当前最新的时间戳
        SingleOutputStreamOperator<SendorReading> resultStream = keyedStream.reduce(new ReduceFunction<SendorReading>() {
            @Override
            public SendorReading reduce(SendorReading currentState, SendorReading newData) throws Exception {
                return new SendorReading(currentState.getName(), newData.getTimestamp(), Math.max(currentState.getTemperature(), newData.getTemperature()));
            }
        });
        resultStream.print();
        //执行任务
        executionEnvironment.execute();
    }
}
