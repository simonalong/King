package com.simon.king.admin.config;

import com.simon.king.core.mq.MqProducer;
import com.simon.king.core.mq.MqProperties;
import com.simon.king.core.mq.entity.MqProducerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 下午7:02
 */
@Configuration
@EnableConfigurationProperties(value = {MqProperties.class})
public class MqConfiguration {

    @Bean(name = "producer", destroyMethod = "shutdown")
    public MqProducer producer(MqProperties mqProperties){
        MqProducerConfig mqProducerConfig = new MqProducerConfig()
            .setProducerGroup("producer_group")
            .setNamesrvAddr(mqProperties.getServerUrl());
        return new MqProducer(mqProducerConfig);
    }
}
