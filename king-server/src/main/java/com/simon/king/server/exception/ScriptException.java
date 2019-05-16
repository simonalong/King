package com.simon.king.server.exception;

/**
 * @author zhouzhenyong
 * @since 2019/1/22 下午2:46
 */
public class ScriptException extends Exception {

    public ScriptException(String message, Throwable throwable){
        super("脚本解析异常：" + message, throwable);
    }
}
