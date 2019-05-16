package com.simon.king.core.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouzhenyong
 * @since 2019/1/16 下午8:06
 */
@Getter
@AllArgsConstructor
public enum RunStatusEnum {
    /**
     * 运行中的类型
     */
    RUNNING("RUNNING", "运行中"),
    /**
     * 任务运行完毕类型
     */
    DONE("DONE", "运行完毕");

    private String name;
    private String desc;
}

