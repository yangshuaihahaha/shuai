package com.shuai.tableapi.udf;

import com.shuai.SendorReading;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;

public class UdfTest1_ScalarFunction {
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

        //5，自定义标量函数，实现求id的hash值
        HashCode hashCode = new HashCode();
        tableEnv.registerFunction("hashCode", hashCode);
        //5.1，Table API
        //需要在环境中注册UDF
        Table resultTable = sensorTable.select("id, ts, hashCode(id)");

        //5.2，SQL
        tableEnv.createTemporaryView("sensor", sensorTable);
        Table resultSQLTable = tableEnv.sqlQuery("select id, ts, hashCode(id) from sensor");


        tableEnv.toAppendStream(resultTable, Row.class).print("resultTable");
        tableEnv.toAppendStream(resultSQLTable, Row.class).print("resultSQLTable");
        env.execute();
    }

    //实现自定义的ScalarFunction
    public static class HashCode extends ScalarFunction {
        public int eval(String str) {
            return str.hashCode();
        }
    }
}
