package com.simon.king.server.service;

import com.alibaba.fastjson.JSON;
import com.simon.king.groovy.NameSpaceInterface;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 名称空间服务
 * @author zhouzhenyong
 * @since 2019/5/20 上午11:40
 */
@Service
public class NameSpaceService implements NameSpaceInterface {

    @Autowired
    private NameDictionary nameDictionary;

    /**
     * 根据命名空间获取对应的某个ip数据
     *
     * 这里暂时随机获取一个，暂时不接入其他的负载均衡策略，这个可以后面去搞
     *
     * @param namespace 命名空间
     * @return 节点中的数据，是
     */
    @Override
    public String getIpAndPort(String namespace){
        if(StringUtils.isEmpty(namespace)){
            return null;
        }
        List<String> dataList = nameDictionary.getNodeList(namespace);
        String data = dataList.get(RandomUtils.nextInt(0, dataList.size()));
        if(!StringUtils.isEmpty(data)){
            IpData ipData = JSON.parseObject(data, IpData.class);
            if(null != ipData){
                return ipData.toHttpFormat();
            }
        }
        return null;
    }

    @Data
    public static class IpData{

        private String ip;
        private String port;

        /**
         * 将数据转换为http的模式
         */
        String toHttpFormat(){
            return "http://" + ip + ":" + port;
        }
    }
}
