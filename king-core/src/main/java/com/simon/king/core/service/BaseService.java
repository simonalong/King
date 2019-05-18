package com.simon.king.core.service;

import static com.simon.neo.util.DateZoneUtil.parseTimeUTC;

import com.simon.neo.NeoMap;
import com.simon.neo.biz.AbstractNeoService;
import java.util.Date;

/**
 * @author zhouzhenyong
 * @since 2019/5/14 下午5:41
 */
public abstract class BaseService extends AbstractNeoService {

    @Override
    public NeoMap insert(NeoMap record) {
        if (!record.containsKey("id")) {
            record.put("id", getNeo().getUid());
        }
        record.put("create_time", new Date());
        record.put("update_time", new Date());
        return super.insert(record);
    }

    @Override
    public NeoMap update(NeoMap record) {
//        parseTimeUTC(record, "create_time");
        record.put("update_time", new Date());
        return super.update(record);
    }
}
