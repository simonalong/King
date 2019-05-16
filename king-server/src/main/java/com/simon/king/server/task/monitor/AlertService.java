package com.simon.king.server.task.monitor;

import com.simon.king.common.util.HttpUtil;
import com.simon.king.server.KingServerConstant;
import com.simon.king.server.util.TimeStrUtil;
import com.simon.neo.NeoMap;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 告警服务
 *
 * @author zhouzhenyong
 * @since 2019/2/27 下午2:45
 */
@Slf4j
@Service
public class AlertService {

    /**
     * 上报告警
     * @param expireTime 已经过期的时间（单位毫秒）
     */
    void alert(TaskMonitorEntity taskMonitorEntity, Long expireTime){
        String alertMessage = "任务超时：\n"
            + "任务Id：" + taskMonitorEntity.getTaskId() + "\n"
            + "任务组：" + taskMonitorEntity.getTaskGroup() + "\n"
            + "任务名字：" + taskMonitorEntity.getTaskName() + "\n"
            + "任务描述：" + taskMonitorEntity.getTaskDesc() + "\n"
            + "执行时间："
            + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(taskMonitorEntity.getCreateTime()))
            + "\n"
            + "超时：" + TimeStrUtil.parseTime(expireTime) + "\n"
            + "Redis中的key：" + KingServerConstant.TASK_MONITOR_KEY;

        log.warn(alertMessage);

        sendAlert(alertMessage);
    }

    private void sendAlert(String content){
        // todo 外部告警，这里进行外接
        try {
            HttpUtil.post("https://xxxxx", NeoMap.of());
        } catch (IOException e) {
            e.printStackTrace();
            log.error("发送任务超时异常");
        }
    }
}
