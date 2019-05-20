package com.simon.king.server;

import com.simon.king.common.util.FileUtil;
import com.simon.king.groovy.ParserService;
import java.io.IOException;
import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhouzhenyong
 * @since 2019/5/18 下午1:08
 */
public class ParserServiceTest extends BaseSpringBootTest{

    @Autowired
    private ParserService parserService;

    /**
     * 执行资源文件路径中的groovy脚本
     */
    private Object runScript(String filePath) throws IOException {
        String script = FileUtil.readFromResource(ParserServiceTest.class, filePath);
        return parserService.parse(script.trim(), "ok");
    }

    @Test
    @SneakyThrows
    public void test1(){
        runScript("/script/base.groovy");
    }

}
