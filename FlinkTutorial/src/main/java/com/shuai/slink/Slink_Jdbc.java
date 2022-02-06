package com.shuai.slink;

import com.shuai.SendorReading;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.http.HttpHost;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class Slink_Jdbc {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        executionEnvironment.setParallelism(1);
        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        DataStream<String> inputStream = executionEnvironment.readTextFile(inputPath);

        DataStream<SendorReading> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
        });
        FlinkJedisPoolConfig config = new FlinkJedisPoolConfig.Builder()
                .setHost("localhost")
                .setPort(6379)
                .build();
        ArrayList<HttpHost> httpHosts = new ArrayList<>();
        httpHosts.add(new HttpHost("localhost", 9200));
        //实现自定义的ES写入操作
        dataStream.addSink(new MyJdbcSink());
        executionEnvironment.execute();
    }

    public static class MyJdbcSink extends RichSinkFunction<SendorReading> {
        //声明连接和预编译语句
        Connection connection = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;

        public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
            //初始化工作，一般是定义状态，或者建立数据库连接
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "123456");
            insertStmt = connection.prepareStatement("insert into sersor_temp (name, temp) values (?, ?)");
            updateStmt = connection.prepareStatement("uodate sersor_temp set temp = ? where name = ?");
        }

        //每来一条数据，调用连接，执行sql
        public void invoke(SendorReading value, Context context) throws  Exception {
            //直接执行更新语句，如果没有更新执行插入
            updateStmt.setDouble(1, value.getTemperature());
            updateStmt.setString(2, value.getName());
            updateStmt.execute();
            if (updateStmt.getUpdateCount() == 0) {
                insertStmt.setString(1, value.getName());
                insertStmt.setDouble(2, value.getTemperature());
                insertStmt.execute();
            }
        }

        //一般是关闭连接或者清空状态
        public void close() throws Exception {
            insertStmt.close();
            updateStmt.close();
            connection.close();
        }
    }
}
