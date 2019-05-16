package com.simon.king.core.mq.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 下午11:16
 */
@Data
@Accessors(chain = true)
public class MqProducerConfig {

    /**
     * 生产者组
     */
    private String producerGroup;
    /**
     * 名字服务地址
     */
    private String namesrvAddr;
    /**
     * 异步情况下发送失败后的重试次数
     */
    private Integer retryTimesWhenSendAsyncFailed = 0;
}
