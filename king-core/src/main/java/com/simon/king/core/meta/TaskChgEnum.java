package com.simon.king.core.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouzhenyong
 * @since 2019/2/13 下午1:56
 */
@Getter
@AllArgsConstructor
public enum TaskChgEnum {
    /**
     * 激活任务
     */
    ACTIVE("ACTIVE","激活"),
    /**
     * 去激活任务
     */
    DE_ACTIVE("DE_ACTIVE","去激活"),
    /**
     * 重新加载
     */
    RELOAD("RELOAD","重新加载"),
    /**
     * 删除任务
     */
    DELETE("DELETE","删除任务");

    private String name;
    private String desc;
}
