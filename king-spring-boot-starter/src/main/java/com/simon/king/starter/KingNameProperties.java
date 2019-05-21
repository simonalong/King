package com.simon.king.starter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouzhenyong
 * @since 2019/5/20 上午11:06
 */
@ConfigurationProperties("king")
public class KingNameProperties {

    @Getter
    @Setter
    private String namespace;
}
