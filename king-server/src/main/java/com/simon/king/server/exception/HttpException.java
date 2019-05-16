package com.simon.king.server.exception;

/**
 * @author zhouzhenyong
 * @since 2019/1/22 下午2:47
 */
public class HttpException extends ScriptException {

    public HttpException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
