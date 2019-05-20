package com.simon.king.common;

/**
 * @author zhouzhenyong
 * @since 2018/11/28 上午10:53
 */
public interface KingConstant {

    /**
     * zk中的节点路径，这是根节点
     */
    String ROOT_PATH = "/king";
    /**
     * zk中的服务节点
     */
    String SERVER_PATH = ROOT_PATH + "/server";
    /**
     * zk中的客户端节点
     */
    String CLIENT_PATH = ROOT_PATH + "/client";
}
