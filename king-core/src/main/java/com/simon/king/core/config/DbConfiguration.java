package com.simon.king.core.config;

import com.simon.neo.Neo;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouzhenyong
 * @since 2018/11/28 上午10:47
 */
@Configuration
public class DbConfiguration {

    @Bean
    public Neo tina(DataSource dataSource){
        Neo tina = Neo.connect(dataSource);
        // 开启分布式id生成器
        tina.openUidGenerator();
        return tina;
    }
}
