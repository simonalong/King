package com.simon.king.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author zhouzhenyong
 * @since 2019/2/12 上午11:23
 */
@Service
public class EnvService {

    @Autowired
    private Environment environment;

    public String getProperty(String path){
        return environment.getProperty(path);
    }

    /**
     * 获取zk的地址，这里有多种方式获取配置地址
     */
    public String getZkAddress(){
        if(null != getProperty("tina.zk.address")){
            return getZkAddress(getProperty("tina.zk.address"));
        } else if (null != getProperty("spring.dubbo.registry.address")){
            return getZkAddress(getProperty("spring.dubbo.registry.address"));
        } else if(null != getProperty("dubbo.registry.address")){
            return getZkAddress(getProperty("dubbo.registry.address"));
        }
        return null;
    }

    /**
     * 对于有些dubbo前面有zk的配置的需要过滤掉
     */
    private String getZkAddress(String path){
        if (path.startsWith("zookeeper://")) {
            return path.substring("zookeeper://".length());
        }
        return path;
    }
}
