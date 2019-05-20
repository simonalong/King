package com.simon.king.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zhouzhenyong
 * @since 2019/5/16 上午11:39
 */
@SpringBootApplication(scanBasePackages={
    "com.simon.king.core",
    "com.simon.king.groovy",
    "com.simon.king.server"})
public class KingServerApplication {

    public static void main(String... args){
        SpringApplication.run(KingServerApplication.class, args);
    }
}
