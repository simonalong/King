package com.simon.king.server.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouzhenyong
 * @since 2019/1/16 下午7:21
 */
@Configuration
public class ScheduleConfig {

    @Bean
    public Scheduler scheduler() throws SchedulerException {
        return new StdSchedulerFactory().getScheduler();
    }
}
