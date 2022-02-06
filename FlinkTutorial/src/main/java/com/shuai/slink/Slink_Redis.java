package com.shuai.slink;

import com.shuai.SendorReading;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

public class Slink_Redis {
    public static void main(String[] args) throws Exception {
        // 创建流处理执行环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        executionEnvironment.setParallelism(1);
        //从文件中读取数据
        String inputPath = "/Users/yangshuai/project/shuai/FlinkTutorial/src/main/resources/sendor.txt";
        DataStream<String> inputStream = executionEnvironment.readTextFile(inputPath);

        DataStream<String> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SendorReading(fields[0], new Long(fields[1]), new Double(fields[2])).toString();
        });
        FlinkJedisPoolConfig config = new FlinkJedisPoolConfig.Builder()
                .setHost("localhost")
                .setPort(6379)
                .build();
        //添加到redis
        dataStream.addSink(new RedisSink(config, new RedisMapper<SendorReading>() {
            //定义保存数据到redis的命令，存成hash表，hset sensor_temp name temperature
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.HSET, "sensor_temp");
            }

            @Override
            public String getKeyFromData(SendorReading data) {
                return data.getName();
            }

            @Override
            public String getValueFromData(SendorReading data) {
                return String.valueOf(data.getTemperature());
            }
        }));
        executionEnvironment.execute();
    }
}
