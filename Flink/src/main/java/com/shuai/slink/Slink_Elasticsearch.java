package com.shuai.slink;

import com.shuai.SendorReading;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.util.ArrayList;
import java.util.HashMap;

public class Slink_Elasticsearch {
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
        dataStream.addSink(new ElasticsearchSink.Builder<SendorReading>(httpHosts, new ElasticsearchSinkFunction<SendorReading>() {
            @Override
            public void process(SendorReading element, RuntimeContext ctx, RequestIndexer indexer) {
                //定义写入的数据spurce
                HashMap<String, String> dataSource = new HashMap<>();
                dataSource.put("name", element.getName());
                dataSource.put("timestamp", String.valueOf(element.getTimestamp()));
                dataSource.put("temperature", String.valueOf(element.getTemperature()));
                //创建请求，作为ed发起的写入命令
                IndexRequest indexRequest = Requests.indexRequest().index("sensor").type("readingdata").source(dataSource);
                //用indexer发送请求
                indexer.add(indexRequest);
            }
        }).build());
        executionEnvironment.execute();
    }
}
