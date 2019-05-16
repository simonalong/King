package com.simon.king.server.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.simon.king.core.dao.TaskDao;
import com.simon.king.core.meta.BizCacheEnum;
import com.simon.neo.NeoMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhouzhenyong
 * @since 2019/1/21 下午5:24
 */
@Slf4j
@Component
public class CacheManager implements InitializingBean {

    @Autowired
    private TaskDao taskDao;
    /**
     * 调度中心的缓存
     */
    private LoadingCache<CacheKey, NeoMap> cacheTask;

    @Override
    public void afterPropertiesSet() {
        initTask();
    }

    /**
     * 初始化配置的缓存
     */
    private void initTask(){
        cacheTask = CacheBuilder.newBuilder()
            // 设置缓存访问后往后延迟1分钟
            .expireAfterAccess(1, TimeUnit.MINUTES)
            // 设置缓存容器的初始容量为20
            .initialCapacity(20)
            // 设置回调
            .build(new CacheLoader<CacheKey, NeoMap>() {
                @Override
                public NeoMap load(@Nonnull CacheKey key) {
                    log.debug("[Tina]：调度任务从DB中获取放入cache，key = {}", key.toString());
                    NeoMap params = key.getParam();
                    if(params.containsKeys("id")){
                        NeoMap record = taskDao.oneIgnoreStatus(params.getLong("id"));
                        if (null == record) {
                            record = NeoMap.of();
                        }
                        return record;
                    } else if(params.containsKeys("task_group", "task_name")){
                        NeoMap record = taskDao.oneIgnoreStatus(params.getStr("task_group"), params.getStr("task_name"));
                        if (null == record) {
                            record = NeoMap.of();
                        }
                        return record;
                    }
                    return NeoMap.of();
                }
            });
    }

    public void delete(BizCacheEnum businessEnum, NeoMap record){
        CacheKey key = CacheKey.of(businessEnum, record);
        switch (businessEnum){
            case TASK:{
                cacheTask.invalidate(key);
                return;
            }
            default:break;
        }
    }

    public NeoMap get(BizCacheEnum businessEnum, NeoMap record) {
        CacheKey key = CacheKey.of(businessEnum, record);
        try {
            switch (businessEnum) {
                case TASK: {
                    return cacheTask.get(key);
                }
                default: break;
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Data
    @RequiredArgsConstructor(staticName = "of")
    public static class CacheKey {
        @NonNull
        private BizCacheEnum businessEnum;
        @NonNull
        private NeoMap param;

        @Override
        public String toString(){
            return businessEnum + "-" + param.toString();
        }
    }
}
