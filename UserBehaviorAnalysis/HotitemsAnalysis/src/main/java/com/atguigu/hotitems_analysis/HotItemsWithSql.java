package com.atguigu.hotitems_analysis;

import com.atguigu.hotitems_analysis.beans.UserBehavior;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Slide;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

public class HotItemsWithSql {
    public static void main(String[] args) throws Exception {
        // 1,创建流处理执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);


        //2，读入文件数据，转换成DataStream
        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/UserBehaviorAnalysis/HotitemsAnalysis/src/main/resources/UserBehavior.csv";
        DataStream<String> inputStream = env.readTextFile(inputPath);

        //3，转换成POJO
        DataStream<UserBehavior> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new UserBehavior(new Long(fields[0]), new Long(fields[1]), new Integer(fields[2]), fields[3], new Long(fields[4]));
        }).assignTimestampsAndWatermarks(new AscendingTimestampExtractor<UserBehavior>() {
            //升序数据设置时间戳和watermark
            @Override
            public long extractAscendingTimestamp(UserBehavior element) {
                return element.getTimestamp() * 1000L;
            }
        });

        //4，创建表执行环境，用blink版本
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

        //5，将流转换成表
        Table dataTable = tableEnv.fromDataStream(dataStream, "itemId, behavior, timestamp.rowtime as ts");

        //6, 分组窗口
        //table api
        Table windowAggTable = dataTable.filter("behavior = 'pv'")
                .window(Slide.over("1.hours").every("5.minutes").on("ts").as("w"))
                .groupBy("itemId, w")
                .select("itemId, w.end as windowEnd, itemId.count as cnt");

        //7，利用开窗函数，对count值进行排序并获取Row number，得到Top n
        //SQL
        DataStream<Row> aggStream = tableEnv.toAppendStream(windowAggTable, Row.class);
        tableEnv.createTemporaryView("agg", aggStream, "itemId, windowEnd, cnt");

        Table resultTable = tableEnv.sqlQuery("select * from (select *, ROW_NUMBER() over (partition by windowEnd order by cnt desc) as row_num from agg) where row_num <= 5");


        //纯sql实现
        tableEnv.createTemporaryView("data_table", dataStream, "itemId, behavior, timestamp.rowtime as ts");
        Table resultSqlTable = tableEnv.sqlQuery("select * from (" +
                "select *, ROW_NUMBER() over (partition by windowEnd order by cnt desc) as row_num from " +
                "(" +
                "select itemId, count(itemId) as cnt, HOP_END(ts, interval '5' minute, interval '1' hour) as windowEnd " +
                "from data_table where behavior = 'pv' " +
                "group by itemId, HOP(ts, interval '5' minute, interval '1' hour)" +
                ")) " +
                "where row_num <= 5");

        tableEnv.toRetractStream(resultTable, Row.class).print();
        tableEnv.toRetractStream(resultSqlTable, Row.class).print();
        env.execute("hot items with sql job");
    }
}
