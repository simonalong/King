package com.simon.king.core.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缓存的业务类型
 * @author zhouzhenyong
 * @since 2019/1/23 下午2:09
 */
@Getter
@AllArgsConstructor
public enum BizCacheEnum {

    /**
     * 配置中心
     */
    CONFIG("CONFIG", "配置中心"),
    /**
     * 调度中心
     */
    TASK("TASK", "调度中心");

    private String name;
    private String desc;
}
