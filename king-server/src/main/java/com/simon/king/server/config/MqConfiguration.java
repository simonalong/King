package com.simon.king.server.config;

import com.simon.king.core.mq.MqConsumer;
import com.simon.king.core.mq.MqProperties;
import com.simon.king.core.mq.entity.MqConsumerConfig;
import com.simon.king.server.listener.TaskChgListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouzhenyong
 * @since 2019/5/16 下午1:40
 */
@Configuration
@EnableConfigurationProperties(value = {MqProperties.class})
public class MqConfiguration {

    @Autowired
    private TaskChgListener taskChgListener;

    @Bean
    public MqConsumerConfig consumerConfig(MqProperties mqProperties){
        return new MqConsumerConfig()
            .setConsumerGroup("consumer_group")
            .setNamesrvAddr(mqProperties.getServerUrl())
            .setSubExpression("*")
            .setTopic("task_chg");
    }

    @Bean
    public MqConsumer consumer(MqConsumerConfig consumerConfig){
        MqConsumer consumer = new MqConsumer(consumerConfig);
        consumer.addListener(taskChgListener);
        return consumer;
    }
}
