package com.simon.king.core.mq;

import com.alibaba.fastjson.JSON;
import com.simon.king.core.meta.TaskChgEnum;
import java.io.UnsupportedEncodingException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * @author zhouzhenyong
 * @since 2019/2/13 下午1:55
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class TaskChgMsg {

    /**
     * 具体的动作String请见{@link TaskChgEnum}
     */
    private TaskChgEnum action;
    /**
     * 任务的数据，不同的情况为不同的类型数据
     * 1.为Long类型，则为DB中的task的Id
     * 2.为Map类型，则表示DB中的数据被删除操作
     */
    private Object taskData;

//    public static TaskChgMsg build(MessageExt data){
//        if(null == data){
//            return new TaskChgMsg();
//        }
//
//        try {
//            return JSON.parseObject(new String(data.getBody(), RemotingHelper.DEFAULT_CHARSET), TaskChgMsg.class);
//        } catch (UnsupportedEncodingException e) {
//            log.error("消息解析失败", e);
//            e.printStackTrace();
//        }
//        return null;
//    }
}
