package com.shuai.tableapi;

import com.shuai.SendorReading;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Over;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.Tumble;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Csv;
import org.apache.flink.table.descriptors.FileSystem;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.types.Row;

public class TableTest5_TimeAndWindow {
    public static void main(String[] args) throws Exception {
        // 1,创建流处理执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);


        //2，读入文件数据，转换成DataStream
        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        DataStream<String> inputStream = env.readTextFile(inputPath);

        //3，转换成POJO
        DataStream<SendorReading> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
        });

        //4，将流转换成表，定义时间属性
        Table dataTable = tableEnv.fromDataStream(dataStream, "id, timestamp as ts, temerature as temp, rt.rowtime");

        //5，窗口操作
        //5.1 Group Window
        //10秒钟时间语义下的滚动窗口//10秒钟时间语义下的滚动窗口
        //table API
        Table resultTable = dataTable.window(Tumble.over("10.seconds").on("rt").as("tw"))
                .groupBy("id, tw")
                .select("id, id.count, temp.avg, tw.end");
        //SQL
        Table resultSQLTable = tableEnv.sqlQuery("select id, count(id) as cnt, avg(temp) as angTemp, tumble_end(rt, interval '10' second) " +
                "from sensor group by id, tumble(rt, interval '10' second)");


        //5.2 Over Window
        Table overResultTable = dataTable.window(Over.partitionBy("id").orderBy("rt").preceding("2.rows").as("ow"))
        .select("id, rt, id.count over ow, temp.avg over ow");
        //SQL
        Table overSQLResultTable = tableEnv.sqlQuery("select id, rt, count(id) over ow, avg(temp) over ow " +
                "from sensor window ow as (partition by id order by rt rows between 2 preceding and current row)");


        tableEnv.toAppendStream(resultTable, Row.class).print("resultTable");
        tableEnv.toRetractStream(resultSQLTable, Row.class).print("resultSQLTable");
        env.execute();
    }
}
