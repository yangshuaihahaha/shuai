package com.shuai.state;

import com.shuai.SendorReading;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;

/*
我们可以利用Keyed State，实现这样一个需求，按照不同的key，检测传感器的温度值，如果连续的两个温度值差是10度，就输出报警
 */
public class State_KeyedState_ApplicationState {
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
        //定义一个有状态的map操作，统计当前sensor数据个数
        SingleOutputStreamOperator<Tuple3<String, Double, Double>> resultStream = dataStream.keyBy("name")
                .flatMap(new TempChangeWarning(10.0));
        resultStream.print();
        executionEnvironment.execute();
    }

    public static class TempChangeWarning extends RichFlatMapFunction<SendorReading, Tuple3<String, Double, Double>> {
        //私有方法，当前温度跳变的阈值
        private Double threshold;

        public TempChangeWarning(Double threshold) {
            this.threshold = threshold;
        }

        //定义状态保存上次的温度值
        private ValueState<Double> lastTempState;

        @Override
        public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
            lastTempState = getRuntimeContext().getState(new ValueStateDescriptor<Double>("last-temp", Double.class));
        }

        @Override
        public void flatMap(SendorReading value, Collector<Tuple3<String, Double, Double>> out) throws Exception {
            //获取上次的温度值
            Double lastTemp = lastTempState.value();
            if (lastTemp != null) {
                Double diff = Math.abs(value.getTemperature() - lastTemp);
                if (diff >= threshold) {
                    out.collect(new Tuple3<>(value.getName(), lastTemp, value.getTemperature()));
                }
            }
            //更新状态
            lastTempState.update(value.getTemperature());
        }

        @Override
        public void close() throws Exception {
            lastTempState.clear();
        }
    }
}
