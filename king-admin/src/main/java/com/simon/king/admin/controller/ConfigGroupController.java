package com.simon.king.admin.controller;

import com.simon.king.admin.constants.AdminConstant;
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
 * @author zhouzhenyong
 * @since 2018/11/28 上午10:47
 */
@Slf4j
@RestController
@RequestMapping(AdminConstant.ADMIN_API_V1 + "/" + "config_group")
public class ConfigGroupController extends BaseResponseController {

    @Autowired
    private ConfigGroupService configGroupService;

    @PostMapping("pageList")
    public ResponseEntity<List> getPageList(@RequestBody NeoMap record) {
        log.debug("请求：" + record);
        List result = configGroupService.getPage(record);
        return ok(result);
    }

    @PostMapping("count")
    public ResponseEntity<Integer> count(@RequestBody NeoMap record) {
        log.debug("请求：" + record);
        return ok(configGroupService.count(record));
    }

    @GetMapping("codeList")
    public ResponseEntity<List> getGroupList() {
        return ok(configGroupService.getStrList());
    }

    @PutMapping("add")
    public ResponseEntity add(@RequestBody NeoMap record) {
        return ok(configGroupService.insert(record));
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ok(configGroupService.delete(id));
    }

    @PostMapping("update")
    public ResponseEntity update(@RequestBody NeoMap record) {
        return ok(configGroupService.update(record));
    }
}
