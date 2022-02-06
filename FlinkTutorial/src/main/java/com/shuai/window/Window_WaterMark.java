package com.shuai.window;

import com.shuai.SendorReading;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.OutputTag;

public class Window_WaterMark {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        //1，设置事件时间语义
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);


        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        DataStream<String> inputStream = env.readTextFile(inputPath);

        DataStream<SendorReading> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
        })      //乱序数据设置时间戳和watermark
                //Time.seconds(2)：设置延迟时间
                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<SendorReading>(Time.seconds(2)) {
                    @Override
                    public long extractTimestamp(SendorReading element) {
                        return element.getTimestamp() * 1000l;
                    }
                });
//                //正常升序数据设置时间戳和watermark
//                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<SendorReading>() {
//                    @Override
//                    public long extractAscendingTimestamp(SendorReading element) {
//                        return element.getTimestamp() * 1000l;
//                    }
//                });
        //基于事件时间开窗聚合，统计15s内温度的最小值
        DataStream<SendorReading> minTempStream = dataStream.keyBy("name")
                .timeWindow(Time.seconds(15))
                .minBy("temperature");
        minTempStream.print("minTemp");

        //基于事件时间开窗聚合，统计15s内温度的最小值。三重保证，保证迟到的数据
        OutputTag<SendorReading> outputTag = new OutputTag<SendorReading>("late");
        SingleOutputStreamOperator<SendorReading> minTempStream2 = dataStream.keyBy("name")
                .timeWindow(Time.seconds(15))
                .allowedLateness(Time.minutes(1))//允许迟到一分钟
                .sideOutputLateData(outputTag)//迟到的数据输出到侧输出流
                .minBy("temperature");
        minTempStream2.print("minTemp");
        minTempStream2.getSideOutput(outputTag).print("late");
    }
}
