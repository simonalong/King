package com.simon.king.common;

/**
 * @author zhouzhenyong
 * @since 2018/11/28 上午10:53
 */
public interface TinaConstant {
    String APP_NAME = "tina";
    String TASK_CHG_TAG = "task_chg";
    String CONFIG_CHG_TAG = "config_chg";

    /**
     * 客户端获取远程配置的Tina的zk地址
     */
    String TINA_ZK_ADDRESS = "tina.zkAddress";
    /**
     * 客户端配置启动的应用名字的key，启动时候作为系统属性-Dtina.group=xxx，如果不设置，则不向Tina服务获取配置
     */
    String TINA_GROUP = "tina.group";
    /**
     * 客户端配置应用的启动配置，启动时候作为系统属性-Dtina.start.key=xxx，如果不设置，则默认为start
     */
    String TINA_START_KEY = "tina.start.key";
    /**
     * 客户端配置应用的启动配置的标签，启动时候作为系统属性-Dtina.start.tag=xxx，如果不设置，则默认为start
     */
    String TINA_START_TAG = "tina.start.tag";
    /**
     * 应用的启动配置的key
     */
    String DEFAULT_START_KEY = "start";
    /**
     * 应用的启动配置的标签
     */
    String DEFAULT_START_TAG = "start";
    /**
     * 基本类型跟复杂类型的区别
     */
    String BASE_TYPE_KEY = "_base_";
    /**
     * 默认的标签值
     */
    String DEFAULT_TAG = "default";
}
