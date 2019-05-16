package com.simon.king.common.exception;

/**
 * @author zhouzhenyong
 * @since 2019/1/18 下午6:31
 */
public class TinaException extends RuntimeException {

    protected TinaException(String message){
        super("[Tina] exception: " + message);
    }

    protected TinaException(String message, Throwable cause) {
        super("[Tina] exception: " + message, cause);
    }

    protected TinaException(Throwable cause){
        super(cause);
    }
}
