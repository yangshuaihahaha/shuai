package com.shuai.transform;

import com.shuai.SendorReading;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;

import java.util.Collections;

/*
连接两条流
 */

public class Transform_ConnectAndCoMap {
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
        DataStream<SendorReading> lowDataStream = splitStream.select("low");

        //合流connect，将高温流转换成二元组类型，与低温流连接合并之后，输出一个状态信息（高温流报警，低温流正常输出）
        DataStream<Tuple2<String, Double>> warningStream = highDataStream.map(new MapFunction<SendorReading, Tuple2<String, Double>>() {
            @Override
            public Tuple2<String, Double> map(SendorReading value) throws Exception {
                return new Tuple2<>(value.getName(), value.getTemperature());
            }
        });

        //
        ConnectedStreams<Tuple2<String, Double>, SendorReading> connectedStreams = warningStream.connect(lowDataStream);
        connectedStreams.map(new CoMapFunction<Tuple2<String, Double>, SendorReading, Object>() {
            @Override
            public Object map1(Tuple2<String, Double> value) throws Exception {
                return new Tuple3<>(value.f0, value.f1, "high temp warning");
            }

            @Override
            public Object map2(SendorReading value) throws Exception {
                return new Tuple2<>(value.getName(), "normal");
            }
        });
        //执行任务
        executionEnvironment.execute();
    }
}
