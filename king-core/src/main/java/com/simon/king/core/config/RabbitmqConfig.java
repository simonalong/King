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
    public Queue taskChg(){
        return new Queue("task_chg");
    }

    @Bean
    public Queue namespace2IpSend(){
        return new Queue("namespace_ip_send");
    }

    @Bean
    public Queue namespace2IpReceive(){
        return new Queue("namespace_ip_receive");
    }
}
