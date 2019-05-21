package com.simon.king.server.task;

import com.simon.king.server.service.ZkParserService;
import com.simon.king.server.task.monitor.TaskMonitorService;
import com.simon.king.server.zk.HashManager;
import com.simon.neo.NeoMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用于管理任务的初始化和控制范围的变化
 * @author zhouzhenyong
 * @since 2019/2/3 下午8:07
 */
@Slf4j
@Service
public class TaskManager implements InitializingBean {

    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private TaskMonitorService monitorService;
    @Autowired
    private ZkParserService zkParserService;
    /**
     * 一致性哈希服务的hash管理器
     */
    private HashManager manager;

    @Override
    public void afterPropertiesSet(){
        registerZKNode();
        taskScheduler.start();
    }

    /**
     * 将当前服务注册到zk
     */
    private void registerZKNode(){
        manager = HashManager.getInstance();
        manager.registerAddAndRemove(
            (k, v) -> {
                log.info("服务" + k + "新增控制范围" + v.toString());
                taskScheduler.submit(v.getFrom(), v.getTo());
            },(k, v) -> {
                log.info("服务" + k + "减少控制范围" + v.toString());
                taskScheduler.shutdown(v.getFrom(), v.getTo());
            }
        );
        // 设置节点最小的作为监控器
        manager.registerGuardHook(() -> {
            log.info("当前应用作为哨兵进行监控");
            monitorService.startMonitor();
        });

        manager.initZookeeper(zkParserService.getZkAddress());
    }

    private void closeNode(){
        manager.close();
    }

    /**
     * 判断该任务是否属于自己管理的范围
     * @return true: 属于， false：不属于
     */
    public boolean taskBelongToSelf(NeoMap record){
        if (!NeoMap.isEmpty(record)){
            if (null != manager){
                return manager.idIsControlled(record.getLong("id"));
            }
        }
        return false;
    }
}
