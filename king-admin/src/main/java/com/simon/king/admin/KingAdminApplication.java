package com.simon.king.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zhouzhenyong
 * @since 2019/5/16 上午11:16
 */
@SpringBootApplication(scanBasePackages={
    "com.simon.king.core",
    "com.simon.king.groovy",
    "com.simon.king.admin"})
public class KingAdminApplication {

    public static void main(String... args) {
        SpringApplication.run(KingAdminApplication.class, args);
    }
}