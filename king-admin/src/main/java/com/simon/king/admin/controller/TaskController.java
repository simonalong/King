package com.simon.king.admin.controller;

import com.simon.king.admin.constants.AdminConstant;
import com.simon.king.admin.service.TaskAdminService;
import com.simon.neo.NeoMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author robot
 */
@Slf4j
@RestController
@RequestMapping(AdminConstant.ADMIN_API_V1 + "/" + "task")
public class TaskController extends BaseResponseController {

    @Autowired
    private TaskAdminService taskService;

    @PutMapping("add")
    public ResponseEntity add(@RequestBody NeoMap record) {
        log.info("增加：" + record);
        return ok(taskService.insert(record));
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        log.info("删除：" + id);
        return ok(taskService.delete(id));
    }

    @PostMapping("update")
    public ResponseEntity update(@RequestBody NeoMap record) {
        log.info("更新：" + record);
        return ok(taskService.update(record));
    }

    @PostMapping("pageList")
    public ResponseEntity<List> pageList(@RequestBody NeoMap record) {
        log.info("查看分页数据：" + record);
        List result = taskService.getPage(record);
        return ok(result);
    }

    @PostMapping("count")
    public ResponseEntity<Integer> count(@RequestBody NeoMap record) {
        log.info("查看总个数：" + record);
        return ok(taskService.count(record));
    }

    @GetMapping("codeList")
    public ResponseEntity<List> getGroupList() {
        return ok(taskService.getCodeList());
    }

    /**
     * 禁用
     */
    @PostMapping("disable")
    public ResponseEntity<Integer> disable(@RequestBody NeoMap record){
        Long id = record.getLong("id");
        log.info("关闭任务：" + id);
        return ok(taskService.disable(id));
    }

    /**
     * 启用
     */
    @PostMapping("enable")
    public ResponseEntity<Integer> enable(@RequestBody NeoMap record){
        Long id = record.getLong("id");
        log.info("打开任务：" + id);
        return ok(taskService.enable(id));
    }

    /**
     * 用于手动触发一次
     */
    @PostMapping("handRun")
    public ResponseEntity<Object> handRun(@RequestBody NeoMap record){
        log.info("手动触发一次：" + record);
        return ok(taskService.handRun(record));
    }

    /**
     * 用于测试执行，失败也要结果
     */
    @PostMapping("run")
    public ResponseEntity<String> run(@RequestBody NeoMap record){
        log.info("脚本执行：" + record);
        return ok(taskService.run(record));
    }
}
