package com.simon.king.core.mq.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 下午11:17
 */
@Data
@Accessors(chain = true)
public class MqConsumerConfig {

    /**
     * 消费组
     */
    private String consumerGroup;
    /**
     * 命名服务地址
     */
    private String namesrvAddr;
    /**
     * 主题
     */
    private String topic;
    /**
     * 子表达式
     */
    private String subExpression;
}
