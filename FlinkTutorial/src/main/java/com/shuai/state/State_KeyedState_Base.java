package com.shuai.state;

import com.shuai.SendorReading;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/*
基本的keyState
 */
public class State_KeyedState_Base {
    public static void main(String[] args) {
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
        SingleOutputStreamOperator<Integer> resultStream = dataStream.keyBy("name")
                .map(new MyKeyCountMapper());
    }

    public static class MyKeyCountMapper extends RichMapFunction<SendorReading, Integer> {
        private ValueState<Integer> keyCountState;
        //其他类型状态的声明
        private ListState<String> myListState;
        private MapState<String, Double> myMapState;
        private ReducingState<SendorReading> readingReducingState;
        @Override
        public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
            keyCountState = getRuntimeContext().getState(new ValueStateDescriptor<Integer>("key-count", Integer.class));
            myListState = getRuntimeContext().getListState(new ListStateDescriptor<String>("my-list", String.class));
            myMapState = getRuntimeContext().getMapState(new MapStateDescriptor<String, Double>("my-map", String.class, Double.class));
            //readingReducingState = getRuntimeContext().getReducingState(new ReducingStateDescriptor<SendorReading>(SendorReading));
        }
        @Override
        public Integer map(SendorReading value) throws Exception {
            Integer count = keyCountState.value();
            count++;
            keyCountState.update(count);

            //其他状态API调用
            //map state
            Iterable<String> strings = myListState.get();
            myListState.add("123");
            //map state
            Double aDouble = myMapState.get("123");
            myMapState.put("123", 1.2);

            myMapState.clear();
            return count;
        }
    }
}
