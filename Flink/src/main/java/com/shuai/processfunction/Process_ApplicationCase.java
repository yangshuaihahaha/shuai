package com.shuai.processfunction;

import com.shuai.SendorReading;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/*
监控传感器温度值，如果温度在10秒中之内（processing time）连续上升，则报警
 */

public class Process_ApplicationCase {
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
                .process(new TempConsIncreWarning(10))
                .print();
        executionEnvironment.execute();
    }

    //实现自定义的处理函数
    public static class TempConsIncreWarning extends KeyedProcessFunction<org.apache.flink.api.java.tuple.Tuple, SendorReading, String> {

        private Integer interval;

        public TempConsIncreWarning(Integer interval) {
            this.interval = interval;
        }

        //定义状态，保存上一次温度值、定时器时间戳
        private ValueState<Double> lastTempState;
        private ValueState<Long> timerTsState;

        public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
            lastTempState = getRuntimeContext().getState(new ValueStateDescriptor<Double>("last-temp", Double.class, Double.MIN_VALUE));
            timerTsState = getRuntimeContext().getState(new ValueStateDescriptor<Long>("last-temp", Long.class));
        }

        @Override
        public void processElement(SendorReading value, Context ctx, Collector<String> out) throws Exception {
            //取出状态
            Double lastTemp = lastTempState.value();
            Long timerTs = timerTsState.value();

            //如果温度上升并且没有定时器，注册10秒后的定时器，开始等待
            if (value.getTemperature() > lastTemp && timerTs == null) {
                //计算出定时器时间戳
                Long ts = ctx.timerService().currentProcessingTime() + interval * 1000L;
                ctx.timerService().registerProcessingTimeTimer(ts);
                timerTsState.update(ts);
            }
            //如果温度下降，删除定时器
            else if (value.getTemperature() < lastTemp && timerTs != null) {
                ctx.timerService().deleteEventTimeTimer(timerTs);
                timerTsState.clear();
            }

            //更新温度状态
            lastTempState.update(value.getTemperature());
        }

        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            //定时器触发
            out.collect("传感器" + ctx.getCurrentKey().getField(0) + "温度值连续" + interval + "秒上升");
            timerTsState.clear();
        }

        public void close() throws Exception {
            lastTempState.clear();
        }
    }
}
