package com.simon.king.core.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务类型
 * @author robot
 */
@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    /**
     * groovy脚本
     */
    GROOVY("GROOVY"),
    /**
     * url链接post方式
     */
    URL("URL"),
;

    private String value;
}