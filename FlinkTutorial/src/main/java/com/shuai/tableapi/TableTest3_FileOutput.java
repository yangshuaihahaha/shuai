package com.shuai.tableapi;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.BatchTableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Csv;
import org.apache.flink.table.descriptors.FileSystem;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.types.Row;

public class TableTest3_FileOutput {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        env.setParallelism(1);

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);


        //2，表的创建：连接外部系统，读取数据
        //2.1，读取文件
        String filePath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        tableEnv.connect(new FileSystem().path(filePath))
        .withFormat(new Csv())
                .withSchema(new Schema()
                        .field("id", DataTypes.STRING())
                        .field("timestamp", DataTypes.BIGINT())
                        .field("temperature", DataTypes.DOUBLE()))
        .createTemporaryTable("inputTable");

        Table inputTable = tableEnv.from("inputTable");
        inputTable.printSchema();
        tableEnv.toAppendStream(inputTable, Row.class);
        env.execute();

        //3, 查询转换
        //3.1， Table API
        //简单转换
        Table resultTable = inputTable.select("id, temperature").filter("id === 'sensor_6'");

        //聚合统计
        Table avgTable = inputTable.groupBy("id").select("id, id.count as count, temperature.avg as avgTemp");

        //3.2 SQL
        tableEnv.sqlQuery("select id, temperature from inputTable where id = 'sensor_6'");
        Table sqlAggTable = tableEnv.sqlQuery("select id, count(id) as cnt, avg(temperature) as avgTemp from inputTable group by id");

        //打印输出
        tableEnv.toAppendStream(resultTable, Row.class).print("resultTable");
        tableEnv.toRetractStream(avgTable, Row.class).print("avgTable");
        tableEnv.toRetractStream(sqlAggTable, Row.class).print("sqlAggTable");

        //4, 输出到文件
        //连接外部文件注册输出表
        String outputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        tableEnv.connect(new FileSystem().path(outputPath))
                .withFormat(new Csv())
                .withSchema(new Schema()
                        .field("id", DataTypes.STRING())
                        .field("temperature", DataTypes.DOUBLE()))
                .createTemporaryTable("outputTable");

        resultTable.insertInto("outputTable");
        env.execute();
    }
}
