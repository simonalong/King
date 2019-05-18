package com.simon.king.core.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouzhenyong
 * @since 2019/5/18 下午2:00
 */
@Configuration
public class RabbitmqConfig {

    @Bean
    public Queue sendDigest(){
        return new Queue("task_chg");
    }
}
