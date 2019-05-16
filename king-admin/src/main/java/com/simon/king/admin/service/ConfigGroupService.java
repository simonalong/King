package com.simon.king.admin.service;

import com.simon.king.core.service.BaseService;
import com.simon.neo.Neo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 下午6:47
 */
@Service
public class ConfigGroupService extends BaseService {

    @Autowired
    private Neo tina;

    @Override
    protected Neo getNeo() {
        return tina;
    }

    @Override
    protected String getTableName() {
        return "t_config_group";
    }
}
