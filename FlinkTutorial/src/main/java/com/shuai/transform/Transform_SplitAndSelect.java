package com.shuai.transform;

import com.shuai.SendorReading;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Collections;

/*
传感器SendorReading的温度有高有地，按照温度的高低（30为界限）拆分成两个流
 */

public class Transform_SplitAndSelect {
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

        SplitStream<SendorReading> splitStream = dataStream.split(new OutputSelector<SendorReading>() {
            @Override
            public Iterable<String> select(SendorReading value) {
                return (value.getTemperature() > 30 ? Collections.singletonList("high") : Collections.singletonList("low"));
            }
        });
        DataStream<SendorReading> highDataStream = splitStream.select("high");
        highDataStream.print();
        DataStream<SendorReading> lowDataStream = splitStream.select("low");
        lowDataStream.print();
        DataStream<SendorReading> allDataStream = splitStream.select("high", "low");
        lowDataStream.print();
        //执行任务
        executionEnvironment.execute();
    }
}
