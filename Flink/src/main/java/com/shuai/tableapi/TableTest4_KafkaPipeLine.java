package com.shuai.tableapi;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Csv;
import org.apache.flink.table.descriptors.FileSystem;
import org.apache.flink.table.descriptors.Kafka;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.types.Row;

public class TableTest4_KafkaPipeLine {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);


        //2，连接kafka，读取数据
        tableEnv.connect(new Kafka()
                .version("0.11")
                .topic("sensor")
                .property("zookeeper.commect", "localhost:2181")
                .property("bootstrap.servers", "localhost:9092"))
        .withFormat(new Csv())
                .withSchema(new Schema()
                        .field("id", DataTypes.STRING())
                        .field("timestamp", DataTypes.BIGINT())
                        .field("temperature", DataTypes.DOUBLE()))
        .createTemporaryTable("inputTable");

        //3, 简单转换
        Table sensorTable = tableEnv.from("inputTable");
        //简单转换
        Table resultTable = sensorTable.select("id, temperature").filter("id === 'sensor_6'");
        //聚合统计
        Table avgTable = sensorTable.groupBy("id").select("id, id.count as count, temperature.avg as avgTemp");
        //3.2 SQL
        Table sqlAllTable = tableEnv.sqlQuery("select id, temperature from inputTable where id = 'sensor_6'");
        Table sqlAggTable = tableEnv.sqlQuery("select id, count(id) as cnt, avg(temperature) as avgTemp from inputTable group by id");
        //打印输出
        tableEnv.toAppendStream(resultTable, Row.class).print("resultTable");
        tableEnv.toRetractStream(avgTable, Row.class).print("avgTable");
        tableEnv.toRetractStream(sqlAggTable, Row.class).print("sqlAggTable");


        //4，建立kafka连接，输出到不同的topic
        tableEnv.connect(new Kafka()
                .version("0.11")
                .topic("sensor")
                .property("zookeeper.commect", "localhost:2181")
                .property("bootstrap.servers", "localhost:9092"))
                .withFormat(new Csv())
                .withSchema(new Schema()
                        .field("id", DataTypes.STRING())
                        .field("temperature", DataTypes.DOUBLE()))
                .createTemporaryTable("outputTable");
        resultTable.insertInto("outputTable");

        env.execute();
    }
}
