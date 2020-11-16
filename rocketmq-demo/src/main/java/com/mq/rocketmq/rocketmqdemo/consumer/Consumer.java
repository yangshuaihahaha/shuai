package com.mq.rocketmq.rocketmqdemo.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

public class Consumer {
    public static void main(String[] args) throws Exception {
        //1.创建消息消费者consumer，并制定消费组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group1");
        //2,指定NameServer地址
        consumer.setNamesrvAddr("192.168.31.136:19876;192.168.31.136:29876");
        //3,启动producer
        consumer.subscribe("base", "Tag1");
        //设置消费模式：负载均衡模式/广播模式(默认是负载均衡)
        consumer.setMessageModel(MessageModel.BROADCASTING);
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt messageExt : msgs) {
                System.out.println(new String(messageExt.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
    }
}
