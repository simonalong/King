package com.simon.king.groovy;

import com.simon.king.groovy.parse.GroovyScriptFactory;
import com.simon.neo.Neo;
import com.simon.neo.NeoMap;
import groovy.lang.Binding;
import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 针对不同的类型进行不同的解析
 *
 * @author zhouzhenyong
 * @since 2019/1/14 上午11:03
 */
@Slf4j
@Service
public class ParserService {

    @Autowired
    private Neo king;
    @Autowired
    private HttpService httpService;
    @Autowired
    private NameSpaceInterface nameSpaceInterface;

    // groovy 脚本引入的jar包，目前groovy脚本中已经默认引入groovy和Java基本的一些包，这里暂时不引入
    private static final String TEMPLAT = ""
        + "import groovy.lang.*\n"
        + "import java.lang.*;\n"
        + "import com.simon.neo.*;\n"
        + "import lombok.extern.slf4j.Slf4j;\n"
        + "\n";

    /**
     * 只要正常情况下的脚本返回值
     */
    public Object parse(String script, Object params) {
        try {
            return GroovyScriptFactory.getInstance().scriptGetAndRun(TEMPLAT + script, new Binding(NeoMap.of("dataMap", init(params))));
        } catch (Exception e) {
            log.error("groovy 脚本执行失败：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 脚本执行异常也需要返回值
     */
    public Object parseAndResult(String script, Object params) {
        try {
            return GroovyScriptFactory.getInstance().scriptGetAndRun(TEMPLAT + script, new Binding(NeoMap.of("dataMap", init(params))));
        } catch (Exception e) {
            log.error("groovy 脚本执行失败：{}", e.getMessage());
            return MessageFormat.format("groovy 脚本执行失败：{0}", e.getMessage());
        }
    }

    /**
     * 将一些服务传入脚本
     */
    private NeoMap init(Object params) {
        return NeoMap.of(
            "db", king,
            "http", httpService,
            "log", log,
            "params", params
        );
    }
}