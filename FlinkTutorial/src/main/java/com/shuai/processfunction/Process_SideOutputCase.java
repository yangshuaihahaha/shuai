package com.shuai.processfunction;

import com.shuai.SendorReading;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/*
侧输出流，监控传感器温度值，将温度值低于30度的数据输出到side output
 */

public class Process_SideOutputCase {
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

        //检查点配置
        //设置检查点间隔：
        executionEnvironment.enableCheckpointing(300);
        //高级选项
        //设置模式
        executionEnvironment.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //设置超时时间
        executionEnvironment.getCheckpointConfig().setCheckpointTimeout(60000L);
        //正在处理的最大的checkpoint
        executionEnvironment.getCheckpointConfig().setMaxConcurrentCheckpoints(2);
        //最小的两个checkpoint间歇时间
        executionEnvironment.getCheckpointConfig().setMinPauseBetweenCheckpoints(2);
        //set whether a job recovery should fallback to checkpoint when there is a more receat savepoint
        executionEnvironment.getCheckpointConfig().setPreferCheckpointForRecovery(true);
        //允许checkpoint失败多少次
        executionEnvironment.getCheckpointConfig().setTolerableCheckpointFailureNumber(2);

        //重启策略配置
        //每个10s重启一次，尝试3次
        executionEnvironment.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 10000L));
        //10分钟之内尝试3次重启，每次间隔1分钟。超过10分钟重启不成功就放弃
        executionEnvironment.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.minutes(10), Time.minutes(1)));


        //定义一个OutputTag，用来表示侧输出流低温流
        OutputTag<SendorReading> lowTempTag = new OutputTag<SendorReading>("lowTemp"){};

        SingleOutputStreamOperator<SendorReading> highTempStream = dataStream.process(new ProcessFunction<SendorReading, SendorReading>() {
            @Override
            public void processElement(SendorReading value, Context ctx, Collector<SendorReading> out) throws Exception {
                //大于30度是高温流，小于30度是低温流
                if (value.getTemperature() > 30) {
                    out.collect(value);
                } else {
                    ctx.output(lowTempTag, value);
                }
            }
        });
        highTempStream.print("high");
        highTempStream.getSideOutput(lowTempTag).print("low-temp");
        executionEnvironment.execute();
    }
}
