package com.simon.king.server.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
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
public class CacheManager {

    @Autowired
    private TaskDao taskDao;
    /**
     * 调度中心的缓存
     */
    private LoadingCache<CacheKey, NeoMap> cacheTask = CacheBuilder
        .newBuilder()
        //设置写缓存后3秒钟过期
        .expireAfterWrite(3, TimeUnit.MINUTES)
        //设置缓存容器的初始容量为10
        .initialCapacity(30)
        //设置要统计缓存的命中率
        .recordStats()
        //设置缓存的移除通知
        .removalListener((RemovalListener<CacheKey, NeoMap>) notification -> log.info(notification.getKey() + " was removed, cause is " + notification.getCause()))
        .build(new CacheLoader<CacheKey, NeoMap>() {
            @Override
            public NeoMap load(CacheKey key) throws Exception {
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

    public void delete(NeoMap record){
        cacheTask.invalidate(CacheKey.of(record));
    }

    public NeoMap get(NeoMap record) {
        try {
            return cacheTask.get(CacheKey.of(record));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Data
    @RequiredArgsConstructor(staticName = "of")
    public static class CacheKey {
        @NonNull
        private NeoMap param;

        @Override
        public String toString(){
            return param.toString();
        }
    }
}
