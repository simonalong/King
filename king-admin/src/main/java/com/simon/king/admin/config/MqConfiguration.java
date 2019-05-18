//package com.simon.king.admin.config;
//
//import com.simon.king.common.KingConstant;
//import com.simon.king.core.mq.MqProperties;
//import org.apache.rocketmq.client.exception.MQClientException;
//import org.apache.rocketmq.client.producer.DefaultMQProducer;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @author zhouzhenyong
// * @since 2019/5/13 下午7:02
// */
//@Configuration
//@EnableConfigurationProperties(value = {MqProperties.class})
//public class MqConfiguration {
//
//    @Bean(name = "producer")
//    public DefaultMQProducer producer(MqProperties mqProperties){
//        DefaultMQProducer producer = new DefaultMQProducer(KingConstant.PRODUCE_GROUP);
//        producer.setNamesrvAddr(mqProperties.getServerUrl());
//        producer.setCreateTopicKey("AUTO_CREATE_TOPIC_KEY");
//        try {
//            producer.start();
//        } catch (MQClientException e) {
//            e.printStackTrace();
//        }
//        return producer;
//    }
//
//}
