package com.shuai.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.concurrent.TimeUnit;

public class Producer {
    public static void main(String[] args) throws Exception {
        //1.创建消息生产者producer，并制定生产组名
        DefaultMQProducer producer = new DefaultMQProducer("group1");
        //2,制定NameServer地址
        producer.setNamesrvAddr("192.168.31.136:19876;192.168.31.136:29876");
        //3,启动producer
        producer.start();
        for (int i = 0; i < 10; i++) {
            //创建消息对象，指定topic，Tag和消息体
            Message msg = new Message("base", "Tag1", ("Hello " + i + 1).getBytes());
            //发送消息
            SendResult result = producer.send(msg);

            System.out.println("发送状态：" + result + ", 消息ID" + result.getMsgId() + ", 队列" + result.getMessageQueue().getQueueId());
            TimeUnit.SECONDS.sleep(1);

        }
        producer.shutdown();
    }
}
