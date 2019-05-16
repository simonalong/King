package com.simon.king.core.mq;

import com.simon.king.core.mq.entity.MqProducerConfig;
import com.simon.king.core.mq.entity.MsgEntity;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 下午5:46
 */
@Slf4j
public class MqProducer {

    private MQProducer producer;

    public MqProducer(MqProducerConfig config){
        producer = ProducerFactory.getProducer(config.getProducerGroup(), config.getNamesrvAddr(), config.getRetryTimesWhenSendAsyncFailed());
        try {
            producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public void sendAsync(MsgEntity msgEntity, Consumer<SendResult> consumer) {
        try {
            producer.send(buildMsg(msgEntity), new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.debug("producer: 发送成功, msgId={}, msg={}", sendResult.getMsgId(), msgEntity.toString());
                    consumer.accept(sendResult);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("producer: 发送失败, msg={}", msgEntity.toString());
                    e.printStackTrace();
                }
            });
        } catch (MQClientException | RemotingException | InterruptedException e) {
            log.error("producer: 发送消息异常, {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public SendResult send(MsgEntity msgEntity){
        try {
            return producer.send(buildMsg(msgEntity));
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("发送失败：msg={}", msgEntity.toString());
            e.printStackTrace();
        }
        return null;
    }

    private Message buildMsg(MsgEntity msgEntity){
        return new Message(msgEntity.getTopic(),
            msgEntity.getTag(),
            msgEntity.getKeys(),
            msgEntity.getMsgByte());
    }

    public void shutdown() {
        producer.shutdown();
    }
}
