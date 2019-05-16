package com.simon.king.server.listener;


import com.simon.king.core.mq.MqConsumer;
import com.simon.king.core.mq.MqListener;
import com.simon.king.core.mq.entity.MqConsumerConfig;

/**
 * @author zhouzhenyong
 * @since 2019/2/14 下午1:44
 */
public class ConfigChgConsumer {

    /**
     * 消费者
     */
    private volatile MqConsumer mqConsumer;

    public void setConsumerConfig(MqConsumerConfig mqConfig, MqListener listener){
        if(null == mqConsumer){
            synchronized (ConfigChgConsumer.class){
                if(null == mqConsumer){
                    mqConsumer = new MqConsumer(mqConfig);
                }
            }
            mqConsumer.addListener(listener);
        }
    }
}
