package com.simon.king.groovy;

/**
 * @author zhouzhenyong
 * @since 2019/5/20 下午5:49
 */
public interface NameSpaceInterface {

    /**
     * 根据命名空间返回对应的ip和端口号
     * @param namespace 命名空间
     * @return 返回带有http全量域名的ip和port，比如:http://xxxx.xx.xxx.xx:port
     */
    String getIpAndPort(String namespace);
}
