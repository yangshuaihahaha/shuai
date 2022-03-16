package com.shuai.transform;

import com.shuai.SendorReading;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class Transform_RichFunction {
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

        SingleOutputStreamOperator<Tuple2<String, Integer>> richStream = dataStream.map(new MyMapper());
        richStream.print();
        //执行任务
        executionEnvironment.execute();
    }

    public static class MyMapper extends RichMapFunction<SendorReading, Tuple2<String, Integer>> {
        @Override
        public Tuple2<String, Integer> map(SendorReading value) throws Exception {
            return new Tuple2<>(value.getName(), getRuntimeContext().getIndexOfThisSubtask());
        }
        public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
            //初始化工作，一般是定义状态，或者建立数据库连接
        }
        public void close() throws Exception {
            //一般是关闭连接或者清空状态
        }
    }
}
