package com.shuai.window;

import com.shuai.SendorReading;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
/*
创建不同类型的窗口
    滚动时间窗口：.timeWindow(Time.seconds(15))
    滑动时间窗口：.timeWindow(Time.seconds(15), Time.seconds(5))
    会话窗口：.window(EventTimeSessionWindows.withGap(Time.minutes(10)))
    滚动计数窗口：.countWindow(5)
    滑动计数窗口：.countWindow(10, 2)
 */

public class Window_Window {
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

        dataStream.keyBy("name")
                .countWindow(15);//滚动计数窗口
//                .countWindow(15,5);//滑动计数窗口
//                .window(EventTimeSessionWindows.withGap(Time.milliseconds(1)));//会话时间窗口
//                .timeWindow(Time.seconds(15), Time.seconds(5));//滑动时间窗口
//                .timeWindow(Time.seconds(15));//滚动时间窗口
//                .window(TumblingProcessingTimeWindows.of(Time.seconds(15)));//时间窗口最原始的方式
    }
}
