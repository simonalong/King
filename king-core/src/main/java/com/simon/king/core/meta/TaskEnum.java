package com.simon.king.core.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouzhenyong
 * @since 2019/1/16 下午8:04
 */
@Getter
@AllArgsConstructor
public enum TaskEnum {
    /**
     * groovy脚本类型
     */
    GROOVY("GROOVY", "groovy脚本"),
    /**
     * url 链接类型
     */
    URL("URL", "url链接");

    private String name;
    private String desc;
}
