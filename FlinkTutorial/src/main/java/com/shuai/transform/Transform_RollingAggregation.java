package com.shuai.transform;

import com.shuai.SendorReading;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class Transform_RollingAggregation {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        executionEnvironment.setParallelism(1);

        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        DataStream<String> inputStream = executionEnvironment.readTextFile(inputPath);

//        //转换成SendorReading类型
//        DataStream<SendorReading> dataStream = inputStream.map(new MapFunction<String, SendorReading>() {
//            public SendorReading map(String value) throws Exception {
//                String[] fields = value.split(",");
//                return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
//            }
//        });

        //使用lamda表达式
        DataStream<SendorReading> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
        });

        //分组
        KeyedStream<SendorReading, Tuple> keyedStream = dataStream.keyBy("name");

        //滚动聚合，获取当前最大值
        DataStream<SendorReading> resultStream = keyedStream.max("temperature");
        resultStream.print();
        //执行任务
        executionEnvironment.execute();
    }
}
