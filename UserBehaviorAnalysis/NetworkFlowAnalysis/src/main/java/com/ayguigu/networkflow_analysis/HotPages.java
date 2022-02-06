package com.ayguigu.networkflow_analysis;

import com.ayguigu.networkflow_analysis.bean.ApacheLogEvent;
import com.ayguigu.networkflow_analysis.bean.PageViewCount;
import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class HotPages {
    public static void main(String[] args) throws Exception {
        // 1,创建流处理执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        //读取文件转换成POJO
        String inputPath = "/Users/yangshuai/project/shuai/UserBehaviorAnalysis/NetworkFlowAnalysis/src/main/resources/apache.log";
        DataStream<String> inputStream = env.readTextFile(inputPath);

        //3，转换成POJO
        DataStream<ApacheLogEvent> dataStream = inputStream.map(line -> {
            String[] fields = line.split(" ");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
            Long timestamp = simpleDateFormat.parse(fields[3]).getTime();
            return new ApacheLogEvent(fields[0], fields[1], timestamp, fields[5], fields[6]);
        }).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<ApacheLogEvent>(Time.minutes(1)) {
            @Override
            public long extractTimestamp(ApacheLogEvent element) {
                return element.getTimestamp();
            }
        });

        //定义一个侧输出流标签
        OutputTag<ApacheLogEvent> lateTag = new OutputTag<>("late");

        //分组开窗聚合
        SingleOutputStreamOperator<PageViewCount> windowAggStream = dataStream.filter(data -> "GET".equals(data.getMethod()))//过滤get请求
                .keyBy(ApacheLogEvent::getUrl)//按照url分组
                .timeWindow(Time.minutes(10), Time.seconds(5))
                .allowedLateness(Time.minutes(1))//允许迟到一分钟
                .sideOutputLateData(lateTag)
                .aggregate(new PageCountAgg(), new PageCountResult());

        DataStream<ApacheLogEvent> sideOutput = windowAggStream.getSideOutput(lateTag);

        //收集同一窗口count数据，排序输出
        windowAggStream.keyBy(PageViewCount::getWindowEnd)
                .process(new TopNHotPages(3));
        env.execute("hot pages job");
    }

    //自定义预聚合函数
    public static class PageCountAgg implements AggregateFunction<ApacheLogEvent, Long, Long> {

        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(ApacheLogEvent value, Long accumulator) {
            return accumulator + 1;
        }

        @Override
        public Long getResult(Long accumulator) {
            return accumulator;
        }

        @Override
        public Long merge(Long a, Long b) {
            return a + b;
        }
    }

    //自定义WindowFunction
    public static class PageCountResult implements WindowFunction<Long, PageViewCount, String, TimeWindow> {
        @Override
        public void apply(String url, TimeWindow window, Iterable<Long> input, Collector<PageViewCount> out) throws Exception {
            out.collect(new PageViewCount(url, window.getEnd(), input.iterator().next()));
        }
    }

    //自定义处理函数
    //自定义WindowFunction
    public static class TopNHotPages extends KeyedProcessFunction<Long, PageViewCount, String> {

        private Integer topsize;

        public TopNHotPages(Integer topsize) {
            this.topsize = topsize;
        }

        //定义状态保存当前所有PageViewCount到list中
        ListState<PageViewCount> pageViewCountListState;

        @Override
        public void open(Configuration parameters) throws Exception {
            pageViewCountListState = getRuntimeContext().getListState(new ListStateDescriptor<>("page-count-list", PageViewCount.class));
        }

        @Override
        public void processElement(PageViewCount value, Context ctx, Collector<String> out) throws Exception {
            pageViewCountListState.add(value);
            ctx.timerService().registerProcessingTimeTimer(value.getWindowEnd() + 1);
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            ArrayList<PageViewCount> pageViewCounts = Lists.newArrayList(pageViewCountListState.get().iterator());
            pageViewCounts.sort((o1, o2) -> o2.getCount().intValue() - o1.getCount().intValue());
            //将排名信息格式化成string，打印输出
            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append("===========================");
            resultBuilder.append("窗口结束时间：").append(new Timestamp(timestamp - 1)).append("\n");

            //遍历列表，取top n输出
            for (int i = 0; i < Math.min(topsize, pageViewCounts.size()); i++) {
                PageViewCount pageViewCount = pageViewCounts.get(i);
                resultBuilder.append("Number").append(i).append(":")
                        .append(" 页面URL = ").append(pageViewCount.getUrl())
                        .append(" 浏览量 = ").append(pageViewCount.getCount())
                        .append("\n");
            }
            resultBuilder.append("===========================\n\n");
            //控制输出频率
            Thread.sleep(1000L);
            out.collect(resultBuilder.toString());
        }
    }
}
