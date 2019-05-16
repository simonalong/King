package com.simon.king.core.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 执行状态
 * @author robot
 */
@Getter
@AllArgsConstructor
public enum RunStatusEnum {

    /**
     * 完成
     */
    DONE("DONE"),
    /**
     * 执行中
     */
    RUNNING("RUNNING"),
;

    private String value;
}