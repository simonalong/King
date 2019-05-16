package com.simon.king.server.exception;


import com.simon.king.common.exception.TinaException;

/**
 * @author zhouzhenyong
 * @since 2019/2/13 下午5:14
 */
public class MqException extends TinaException {
    public MqException(String message, Throwable cause) {
        super(message, cause);
    }
}
