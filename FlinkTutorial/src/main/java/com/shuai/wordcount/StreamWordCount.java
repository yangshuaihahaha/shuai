package com.shuai.wordcount;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class StreamWordCount {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        executionEnvironment.setParallelism(4);

        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/hello.txt";
        DataStream<String> inputDataStream = executionEnvironment.readTextFile(inputPath);
        //基于数据流进行转换计算
        //对数据进行处理，按空格分词展开，转换成(word， 1)二元组进行统计
        DataStream<Tuple2<String, Integer>> resultStream = inputDataStream.flatMap(new MyFlatMapper()).slotSharingGroup("red")
                .keyBy(0)//按照第一个位置的word分组
                .sum(1).slotSharingGroup("green");//将第二个位置上的数据求和
        resultStream.print();

        //执行任务
        executionEnvironment.execute();
    }

    public static class MyFlatMapper implements FlatMapFunction<String, Tuple2<String, Integer>> {

        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            //按空格分词
            String[] words = value.split(" ");
            //遍历所有word，包成二元组输出
            for (String word : words) {
                out.collect(new Tuple2<String, Integer>(word, 1));
            }
        }
    }
}
