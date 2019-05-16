package com.simon.king.server.exception;

/**
 * @author zhouzhenyong
 * @since 2019/1/18 下午6:29
 */
public class CronParseException extends Exception {

    public CronParseException(String message){
        super(message);
    }

    public CronParseException(String message, Throwable throwable){
        super("cron表达式解析异常", throwable);
    }
}
