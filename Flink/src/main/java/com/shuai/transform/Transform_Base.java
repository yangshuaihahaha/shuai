package com.shuai.transform;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.util.Random;
import java.util.logging.Filter;

public class Transform_Base {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        executionEnvironment.setParallelism(4);

        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/hello.txt";
        DataStream<String> inputStream = executionEnvironment.readTextFile(inputPath);
        //1，map把String转换成长度输出
        DataStream<Integer> mapStream = inputStream.map(new MapFunction<String, Integer>() {
            public Integer map(String value) throws Exception {
                return value.length();
            }
        });
        //2, flatMap 按逗号区分
        DataStream<String> flatMapStream = inputStream.flatMap(new FlatMapFunction<String, String>() {
            public void flatMap(String value, Collector<String> out) throws Exception {
                String[] fields = value.split(",");
                for (String field : fields) {
                    out.collect(field);
                }
            }
        });
        //3，filter，筛选asd开头的数据
        DataStream<String> filterStream = inputStream.filter(new FilterFunction<String>() {
            public boolean filter(String value) throws Exception {
                return value.startsWith("asd");
            }
        });

        filterStream.print();

        //执行任务
        executionEnvironment.execute();
    }
}
