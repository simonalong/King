package com.simon.king.server.service;

import com.alibaba.fastjson.JSONObject;
import com.like.tina.common.util.PropertiesUtil;
import com.like.tina.common.util.RecordUtils;
import com.like.tina.common.util.YmlUtil;
import com.like.tina.core.dao.ConfigItemDao;
import com.like.tina.core.mq.ConfigChgMsg;
import com.like.tina.core.service.ConfigItemService;
import com.like.tina.server.cache.ConfigCacheHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import me.zzp.am.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author zhouzhenyong
 * @since 2018/12/29 下午3:26
 */
@Slf4j
@Service
public class ConfigService extends ConfigItemService {

    @Autowired
    private ConfigItemDao configItemDao;
    @Autowired
    private ConfigCacheHelper configCacheHelper;

    @Override
    public Integer delete(Long id){
        Record record = one(id);
        Integer result = super.delete(id);
        if(!RecordUtils.isEmpty(record)){
            if (1 == result) {
                configCacheHelper.delete(record);
            }
        }
        return result;
    }

    @Override
    public Integer update(Record record){
        Integer result = getDao().update(record);
        if(1 == result){
            configCacheHelper.delete(record);
        }
        return result;
    }

    public Record one(String group, String key, String tag){
        return configCacheHelper.get(group, key, tag);
    }

    /**
     * server 这边只接收更新和删除，用于删除本机的内部缓存
     */
    @SuppressWarnings("unchecked")
    public void accept(ConfigChgMsg configChgMsg){
        Record record = Record.from(configChgMsg.getData());
        switch (configChgMsg.getAction()) {
            case UPDATE:
            case DELETE: {
                configCacheHelper.delete(record);
            } break;
            case INSERT:break;
            default:break;
        }
    }

    public List<Record> getValueList(String group){
        List<Record> records = configItemDao.getValueList(group);
        if (CollectionUtils.isEmpty(records)){
            return Collections.emptyList();
        }

        return records;
    }

    private Record buildValueEntity(Record record){
        if(null == record || record.isEmpty()){
            return null;
        }

        Map<String, Object> dataMap = new HashMap<>();
        String content = record.getStr("conf_value");

        switch (record.getStr("val_type")){
            case "STRING":{
                dataMap = Record.of("_base_", content);
            }break;
            case "JSON":{
                dataMap = JSONObject.parseObject(content);
            }break;
            case "YML":{
                dataMap = YmlUtil.ymlToMap(content);
            }break;
            case "PROPERTY":{
                dataMap = PropertiesUtil.property2Map(content);
            }break;
            default:break;
        }

        assert dataMap != null;
        return Record.of(dataMap);
    }
}
