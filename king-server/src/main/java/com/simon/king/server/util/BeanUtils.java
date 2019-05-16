package com.simon.king.server.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author zhouzhenyong
 * @since 2019/1/18 下午5:37
 */
@Component
public class BeanUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BeanUtils.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String name){
        if (null != applicationContext) {
            return applicationContext.getBean(name);
        }
        return null;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (null != applicationContext) {
            return applicationContext.getBean(clazz);
        }
        return null;
    }
}
