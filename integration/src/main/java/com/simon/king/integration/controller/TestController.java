package com.simon.king.integration.controller;

import com.simon.neo.NeoMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouzhenyong
 * @since 2019/5/20 下午5:31
 */
@Slf4j
@RestController
@RequestMapping("test")
public class TestController {

    @GetMapping(value = "get/{women}")
    public String get(@PathVariable String women){
        return women + "ddd";
    }

    @PostMapping(value = "post")
    public String post(@RequestBody NeoMap dataMap){
        return dataMap.toString();
    }

    @RequestMapping(value = "head/{id}", method=RequestMethod.HEAD)
    public String head(@RequestBody NeoMap dataMap){
        return dataMap.toString();
    }

    @DeleteMapping(value = "delete/{id}")
    public String delete(@PathVariable String id){
        return id + "-ddd";
    }

    @PutMapping(value = "put")
    public String put(@RequestBody NeoMap dataMap){
        return dataMap.toString();
    }

    @PatchMapping(value = "patch")
    public String patch(@RequestBody NeoMap dataMap){
        return dataMap.toString();
    }
}
