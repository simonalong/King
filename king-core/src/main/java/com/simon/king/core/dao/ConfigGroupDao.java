package com.simon.king.core.dao;

import com.simon.neo.Neo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 上午11:37
 */
@Repository
public class ConfigGroupDao {

    private static final String TABLE_NAME = "t_config_group";

    @Autowired
    private Neo tina;

    public List<String> getAllCodeList(){
        return tina.values(TABLE_NAME, "group_code");
    }
}
