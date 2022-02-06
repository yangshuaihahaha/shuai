package com.atguigu.hotitems_analysis;

import com.atguigu.hotitems_analysis.beans.ItemViewCount;
import com.atguigu.hotitems_analysis.beans.UserBehavior;
import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.util.ArrayList;

public class HotItems {
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

        //4，分组开窗聚合，得到每个窗口内各个商品的count值
        DataStream<ItemViewCount> windowAggStream = dataStream
                .filter(data -> "pv".equals(data.getBehavior()))//过滤pv行为
                .keyBy("itemId")//按照商品id分组
                .timeWindow(Time.hours(1), Time.minutes(5))//开一个1小时的滑动窗口，各每隔5分钟滑动一次
                .aggregate(new ItemCountAgg(), new WindowItemCountResult());//定义一个增量聚合函数。后面跟一个全窗口函数，包装成我们想要的结果。

        //5，收集同一窗口的所有商品count数据，排序输出top n
        DataStream<String> resultStream = windowAggStream.keyBy("windowEnd")
                .process(new TopNHotItems(5));
        resultStream.print();
        env.execute("hot items analysis");
    }

    //实现自定义增量聚合函数
    public static class ItemCountAgg implements AggregateFunction<UserBehavior, Long, Long> {

        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(UserBehavior value, Long accumulator) {
            return accumulator + 1;
        }

        @Override
        public Long getResult(Long accumulator) {
            return accumulator;
        }
        /*
        只有会话窗口SessionWindow会调用该方法，如果是TimeWindow是不会调用的，merage方法即使返回null也是可以的
        因为会话窗口没有固定的起始时间和结束时间，他们被运算于不同的滚动窗口和滑动窗口上。本质上，
        会话窗口会为每一批相邻两条数据没有大于指定间隔时间的数据merage到一起。
        为了数据能够被merage，会话窗口需要一个merage的触发器和一个可以merage的WindowFunction，比如ReduceFunction、AggreateFunction或者ProcessWindowFunction，需要注意的是FlodFunction不能merage
         */
        @Override
        public Long merge(Long a, Long b) {
            return a + b;
        }
    }

    //实现自定义全窗口函数
    public static class WindowItemCountResult implements WindowFunction<Long, ItemViewCount, Tuple, TimeWindow> {

        @Override
        public void apply(Tuple tuple, TimeWindow window, Iterable<Long> input, Collector<ItemViewCount> out) throws Exception {
            Long itemId = tuple.getField(0);
            Long windowEnd = window.getEnd();
            Long count = input.iterator().next();
            out.collect(new ItemViewCount(itemId, windowEnd, count));
        }
    }


    //实现自定义的KeyedProcessFunction
    public static class TopNHotItems extends KeyedProcessFunction<Tuple, ItemViewCount, String> {
        //定义属性Top n 大小
        private Integer topSize;

        public TopNHotItems(Integer topSize) {
            this.topSize = topSize;
        }

        //定义列表状态，保存当前窗口内所有输出的ItemViewCount
        ListState<ItemViewCount> itemViewCountListState;

        @Override
        public void open(Configuration parameters) throws Exception {
            itemViewCountListState = getRuntimeContext().getListState(new ListStateDescriptor<ItemViewCount>("item-view-count-list", ItemViewCount.class));
        }

        @Override
        public void processElement(ItemViewCount value, Context ctx, Collector<String> out) throws Exception {
            //每来一条数据，存入list。并注册定时器
            itemViewCountListState.add(value);
            ctx.timerService().registerProcessingTimeTimer(value.getWindowEnd() + 1);
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            //定时器触发，当前已收集到所有数据，排序并输出
            ArrayList<ItemViewCount> itemViewCounts = Lists.newArrayList(itemViewCountListState.get().iterator());
            itemViewCounts.sort((o1, o2) -> o2.getCount().intValue() - o1.getCount().intValue());

            //将排名信息格式化成string，打印输出
            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append("===========================");
            resultBuilder.append("窗口结束时间：").append(new Timestamp(timestamp - 1)).append("\n");

            //遍历列表，取top n输出
            for (int i = 0; i < Math.min(topSize, itemViewCounts.size()); i++) {
                ItemViewCount currentItemViewCount = itemViewCounts.get(i);
                resultBuilder.append("Number").append(i).append(":")
                        .append(" 商品ID = ").append(currentItemViewCount.getItemId())
                        .append(" 热门度 = ").append(currentItemViewCount.getCount())
                        .append("\n");
            }
            resultBuilder.append("===========================\n\n");
            //控制输出频率
            Thread.sleep(1000L);
            out.collect(resultBuilder.toString());
        }
    }
}
