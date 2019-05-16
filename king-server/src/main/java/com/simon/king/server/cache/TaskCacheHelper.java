package com.simon.king.server.cache;

import com.simon.king.core.meta.BizCacheEnum;
import com.simon.neo.NeoMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhouzhenyong
 * @since 2019/1/23 下午3:10
 */
@Component
public class TaskCacheHelper {

    @Autowired
    private CacheManager cacheManager;
    private static final String GROUP = "task_group";
    private static final String KEY = "task_name";
    private static final String ID = "id";

    public NeoMap get(Long id){
        return cacheManager.get(BizCacheEnum.TASK, NeoMap.of(ID, id));
    }

    public NeoMap get(String taskGroup, String taskName){
        return cacheManager.get(BizCacheEnum.TASK, NeoMap.of(GROUP, taskGroup, KEY, taskName));
    }

    private void delete(Long id){
        cacheManager.delete(BizCacheEnum.TASK, NeoMap.of(ID, id));
    }

    private void delete(String taskGroup, String taskName){
        cacheManager.delete(BizCacheEnum.TASK, NeoMap.of(GROUP, taskGroup, KEY, taskName));
    }

    public void delete(NeoMap record){
        delete(record.getLong("id"));
        delete(record.getStr(GROUP), record.getStr(KEY));
    }
}
