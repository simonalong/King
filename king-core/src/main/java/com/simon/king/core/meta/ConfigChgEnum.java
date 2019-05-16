package com.simon.king.core.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouzhenyong
 * @since 2019/2/14 下午4:44
 */
@Getter
@AllArgsConstructor
public enum ConfigChgEnum {

    /**
     * 配置新增
     */
    INSERT("INSERT","新增"),
    /**
     * 配置更新
     */
    UPDATE("UPDATE","更新"),
    /**
     * 配置删除
     */
    DELETE("DELETE","删除");

    private String name;
    private String desc;
}
