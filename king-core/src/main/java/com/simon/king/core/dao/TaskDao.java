package com.simon.king.core.dao;

import com.simon.neo.Neo;
import com.simon.neo.NeoMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author robot
 */
@Repository
public class TaskDao {

    private static final String TABLE_NAME = "t_task";

    @Autowired
    private Neo tina;

    public NeoMap one(Long id){
        return tina.one(TABLE_NAME, id);
    }

    public NeoMap oneIgnoreStatus(Long id){
        if (null != id) {
            return tina.one(TABLE_NAME, id);
        }
        return null;
    }

    public NeoMap one(String taskGroup, String taskName){
        return tina.one(TABLE_NAME, NeoMap.of("task_group", taskGroup, "task_name", taskName, "status", "Y"));
    }

    public NeoMap oneIgnoreStatus(String taskGroup, String taskName){
        return tina.one(TABLE_NAME, NeoMap.of("task_group", taskGroup, "task_name", taskName));
    }

    public List<String> getCodeList(){
        return tina.values(TABLE_NAME, "task_group");
    }
}
