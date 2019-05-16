package com.simon.king.core.mq.entity;

import java.io.UnsupportedEncodingException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 下午9:46
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"msgByte"})
@Accessors(chain = true)
public class MsgEntity {

    private String topic;
    private String tag;
    private String keys;
    private byte[] msgByte;

    public MsgEntity setMessage(String message){
        try {
            this.msgByte = message.getBytes(RemotingHelper.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }
}
