package com.simon.king.admin;

import com.alibaba.fastjson.JSON;
import com.simon.king.common.KingConstant;
import com.simon.king.core.meta.TaskChgEnum;
import com.simon.king.core.meta.TaskEntity;
import com.simon.king.core.mq.TaskChgMsg;
import java.io.UnsupportedEncodingException;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.junit.Test;

/**
 * @author zhouzhenyong
 * @since 2019/5/14 下午10:59
 */
@Slf4j
public class ProducerTest {

    @Test
    @SneakyThrows
    public void sendTest(){
        TaskChgMsg taskChgMsg = new TaskChgMsg()
            .setAction(TaskChgEnum.ACTIVE)
            .setTaskData(new TaskEntity().setData("sdf"));

        DefaultMQProducer producer = new DefaultMQProducer(KingConstant.PRODUCE_GROUP);
        producer.setNamesrvAddr("localhost:9876");
        producer.start();

        SendResult result = producer.send(buildTaskChgMsg(taskChgMsg));
        if(null != result){
            log.info("发送成功，transactionId = {}, msgId={}, msg={}", result.getTransactionId(), result.getMsgId(), taskChgMsg);
        }
    }

    private Message buildTaskChgMsg(TaskChgMsg taskChgMsg) {
        try {
            return new Message(KingConstant.TOPIC, KingConstant.TAG, "*",
                JSON.toJSONString(taskChgMsg).getBytes(RemotingHelper.DEFAULT_CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    @SneakyThrows
    public void sendTest2(){
        DefaultMQProducer producer = new
            DefaultMQProducer("produce_group");
        producer.setNamesrvAddr("localhost:9876");
        producer.start();
        for (int i = 0; i < 100; i++) {
            //Create a message instance, specifying topic, tag and message body.
            Message msg = new Message("TopicTest" /* Topic */,
                "TagA" /* Tag */,
                ("Hello RocketMQ " +
                    i).getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
            );
            //Call send message to deliver message to one of brokers.
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n", sendResult);
        }
        //Shut down once the producer instance is not longer in use.
        producer.shutdown();
    }

    @Test
    @SneakyThrows
    public void receiveTest1(){
        // Instantiate with specified consumer group name.
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_group");

        // Specify name server addresses.
        consumer.setNamesrvAddr("localhost:9876");

        // Subscribe one more more topics to consume.
        consumer.subscribe("TopicTest", "*");
        // Register callback to execute on arrival of messages fetched from brokers.
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                ConsumeConcurrentlyContext context) {
                System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        //Launch the consumer instance.
        consumer.start();

        System.out.printf("Consumer Started.%n");
    }
}
