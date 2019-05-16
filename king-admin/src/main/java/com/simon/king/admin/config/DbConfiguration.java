package com.simon.king.admin.config;

import com.simon.neo.Neo;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouzhenyong
 * @since 2019/5/16 上午11:19
 */
@Configuration
public class DbConfiguration {

    @Bean
    public Neo tina(@Qualifier("dataSource") DataSource dataSource){
        return Neo.connect(dataSource);
    }
}
