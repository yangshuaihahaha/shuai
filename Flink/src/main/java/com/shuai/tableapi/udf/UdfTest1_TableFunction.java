package com.shuai.tableapi.udf;

import com.shuai.SendorReading;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;
import scala.Tuple2;

public class UdfTest1_TableFunction {
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

        //5，自定义表函数，将id拆分，并输出（word，length）
        Split split = new Split();
        //需要在环境中注册UDF
        tableEnv.registerFunction("split", split);


        //5.1，Table API
        Table resultTable = sensorTable.joinLateral("split(id) as (word, length)").select("id, ts, word, length");

        //5.2，SQL
        tableEnv.createTemporaryView("sensor", sensorTable);
        Table resultSQLTable = tableEnv.sqlQuery("select id, ts, word, length from sensor, lateral table(split(id)) as splitid(word, length)");


        tableEnv.toAppendStream(resultTable, Row.class).print("resultTable");
        tableEnv.toAppendStream(resultSQLTable, Row.class).print("resultSQLTable");
        env.execute();
    }

    //实现自定义的TableFunction
    public static class Split extends TableFunction<Tuple2<String, Integer>> {
        //eval方法是必须的，没有返回值
        public void eval(String str) {
            for (String s : str.split("-")) {
                collect(new Tuple2<>(s, s.length()));
            }
        }
    }
}
