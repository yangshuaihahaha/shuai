package com.shuai.tableapi.udf;

import com.shuai.SendorReading;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.types.Row;

public class UdfTest1_AggregateFunction {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        env.setParallelism(1);
        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        DataStream<String> inputStream = env.readTextFile(inputPath);
        //转换成POJO
        DataStream<SendorReading> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
        });
        //3，创建表环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //4，流转换成表
        Table sensorTable = tableEnv.fromDataStream(dataStream, "id, timestamp as ts, temperature as temp");

        //5，自定义聚合函数，求当前传感器的平均温度值
        AvgTemp avgTemp = new AvgTemp();
        tableEnv.registerFunction("avgTemp", avgTemp);
        //5.1，Table API
        //需要在环境中注册UDF
        Table resultTable = sensorTable
                .groupBy("id")
                .aggregate("avgTemp(temp) as avgTemp")
                .select("id, avgTemp");

        //5.2，SQL
        tableEnv.createTemporaryView("sensor", sensorTable);
        Table resultSQLTable = tableEnv.sqlQuery("select id, avgTemp(temp) from sensor group by id");


        tableEnv.toRetractStream(resultTable, Row.class).print("resultTable");
        tableEnv.toRetractStream(resultSQLTable, Row.class).print("resultSQLTable");
        env.execute();
    }

    //实现自定义的AggregateFunction
    public static class AvgTemp extends AggregateFunction<Double, Tuple2<Double, Integer>> {

        @Override
        public Double getValue(Tuple2<Double, Integer> accumulator) {
            return accumulator.f0 / accumulator.f1;
        }

        @Override
        public Tuple2<Double, Integer> createAccumulator() {
            return new Tuple2<>(0.0, 0);
        }

        //必须实现一个accumulator方法，来数据之后更新状态
        public void accumulate(Tuple2<Double, Integer> accumulator, Double temp) {
            accumulator.f0 += temp;
            accumulator.f1 += 1;
        }
    }
}
