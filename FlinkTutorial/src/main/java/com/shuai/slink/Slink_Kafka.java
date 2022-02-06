package com.shuai.slink;

import com.shuai.SendorReading;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;

public class Slink_Kafka {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        executionEnvironment.setParallelism(1);
        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        DataStream<String> inputStream = executionEnvironment.readTextFile(inputPath);

        DataStream<String> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2])).toString();
        });
        //添加到kafka
        dataStream.addSink(new FlinkKafkaProducer011<>("localhost:9092", "slinktest", new SimpleStringSchema()));
        executionEnvironment.execute();
    }
}
