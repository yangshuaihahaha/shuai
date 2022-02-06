package com.shuai.tableapi;

import com.shuai.SendorReading;
import com.shuai.window.Window_Count;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

public class TableTest1_Example {
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

        //4，基于流创建一张表
        Table dataTable = tableEnv.fromDataStream(dataStream);

        //5,调用table api进行转换操作
        Table resultTable = dataTable.select("id, temperature").where("id = 'sensor_1'");

        //6,执行sql
        tableEnv.createTemporaryView("sensor", dataTable);
        String sql = "select id, temperature from sensor where id='sensor_1'";
        Table resultSqlTable = tableEnv.sqlQuery(sql);

        tableEnv.toAppendStream(resultTable, Row.class).print("resultTable");
        tableEnv.toAppendStream(resultSqlTable, Row.class).print("resultSqlTable");

        env.execute();
    }
}
