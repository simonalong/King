package com.simon.king.server.cache;

import com.simon.king.core.meta.BizCacheEnum;
import com.simon.neo.NeoMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhouzhenyong
 * @since 2019/1/23 下午2:52
 */
@Component
public class ConfigCacheHelper {

    @Autowired
    private CacheManager cacheManager;
    private static final String GROUP = "group_code";
    private static final String KEY = "conf_key";
    private static final String TAG = "tag";

    public NeoMap get(String group, String key, String tag){
        return cacheManager.get(BizCacheEnum.CONFIG, NeoMap.of(GROUP, group, KEY, key, TAG, tag));
    }

    public NeoMap get(NeoMap record){
        return get(record.getStr(GROUP), record.getStr(KEY), record.getStr(TAG));
    }

    public void delete(String group, String key, String tag){
        cacheManager.delete(BizCacheEnum.CONFIG, NeoMap.of(GROUP, group, KEY, key, TAG, tag));
    }

    public void delete(NeoMap record){
        delete(record.getStr(GROUP), record.getStr(KEY), record.getStr(TAG));
    }
}
