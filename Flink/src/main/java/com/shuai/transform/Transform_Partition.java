package com.shuai.transform;

import com.shuai.SendorReading;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class Transform_Partition {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        executionEnvironment.setParallelism(4);

        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        DataStream<String> inputStream = executionEnvironment.readTextFile(inputPath);
        inputStream.print("input");

        //1，shuffle
        DataStream<String> shuffleStream = inputStream.shuffle();
        shuffleStream.print("shuffle");

        DataStream<SendorReading> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
        });
        //2，key by（相同的name都在一个分区上面）
        //
        dataStream.keyBy("name").print("keyBy");
        //3，global（那所有的数据发送到下游的第一个分区上）
        dataStream.global().print("global");
        //执行任务
        executionEnvironment.execute();
    }
}
