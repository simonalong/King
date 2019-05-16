package com.simon.king.core.mq.entity;

import com.alibaba.fastjson.JSON;
import java.io.UnsupportedEncodingException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 下午10:03
 */
@Data
@Accessors(chain = true)
public class MqMessage {

    private String id;
    private String tag;
    private String business;
    private String data;

    public static MqMessage build(MessageExt data) {
        if (null == data) {
            return new MqMessage();
        }

        try {
            return JSON.parseObject(new String(data.getBody(), RemotingHelper.DEFAULT_CHARSET), MqMessage.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
