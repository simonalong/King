package com.simon.king.server.task.monitor;

import com.alibaba.fastjson.JSONObject;
import com.simon.king.server.KingServerConstant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 任务监控模块
 *
 * @author zhouzhenyong
 * @since 2019/2/27 下午2:42
 */
@Slf4j
@Service
public class TaskMonitorService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AlertService alertService;

    private static final String LOG_PRE = "[Tina-monitor]";

    /**
     * 设置10s定时监控
     */
    private static final Integer TIME_NUM = 10;

    /**
     * 过期时间长度，这里默认设置为5秒，如果任务超过5秒还没有执行完毕，则需要上报告警
     */
    private static final Integer EXPIRE_TIME = 5;

    /**
     * 任务监控的执行超时守护线程
     */
    private ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(3, r -> {
        Thread thread = new Thread(r, "[Thread-task-alert]");
        thread.setDaemon(true);
        return thread;
    });

    public void startMonitor() {
        // 上一次任务执行完之后再往后延迟5s执行下一次任务
        scheduler.scheduleWithFixedDelay(() -> CompletableFuture.runAsync(this::runSchedule), 10, TIME_NUM,
            TimeUnit.SECONDS);
    }

    private void runSchedule() {
        try {
            log.debug(LOG_PRE + "任务过期检测");
            SetOperations<String, String> setOperations = redisTemplate.opsForSet();
            Set<String> monitorSets = setOperations.members(KingServerConstant.TASK_MONITOR_KEY);
            if (!CollectionUtils.isEmpty(monitorSets)) {
                monitorSets.forEach(m -> {
                    TaskMonitorEntity entity = JSONObject.parseObject(m, TaskMonitorEntity.class);
                    long expireTime = getExpire(entity);
                    if (expireTime > 0) {
                        alertService.alert(entity, expireTime + TimeUnit.SECONDS.toMillis(EXPIRE_TIME));
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取过期时间
     *
     * @return 返回一个long，大于0表示已经超过过期时间限度（单位毫秒）
     */
    private long getExpire(TaskMonitorEntity taskMonitorEntity) {
        if (null != taskMonitorEntity && null != taskMonitorEntity.getCreateTime()) {
            return System.currentTimeMillis() - (taskMonitorEntity.getCreateTime() + TimeUnit.SECONDS
                .toMillis(EXPIRE_TIME));
        }
        return 0;
    }
}
