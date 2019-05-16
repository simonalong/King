package com.simon.king.core.mq;

import com.alibaba.fastjson.JSON;
import com.simon.king.core.meta.TaskChgEnum;
import com.simon.king.core.mq.entity.MqMessage;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.rocketmq.common.message.Message;

/**
 * @author zhouzhenyong
 * @since 2019/2/13 下午1:55
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class TaskChgMsg extends Message {

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

    public static TaskChgMsg build(MqMessage data){
        if(null == data){
            return new TaskChgMsg();
        }

        TaskChgEnum taskChgEnum = TaskChgEnum.valueOf(data.getBusiness());
        TaskChgMsg taskChgMsg = new TaskChgMsg().setAction(taskChgEnum);

        if (taskChgEnum.equals(TaskChgEnum.DELETE)){
            taskChgMsg.setTaskData(JSON.parseObject(data.getData(), Map.class));
        }else{
            taskChgMsg.setTaskData(JSON.parseObject(data.getData(), Long.class));
        }
        return taskChgMsg;
    }
}
