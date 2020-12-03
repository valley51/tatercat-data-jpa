package com.tatercat;

import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * @Author guk
 * @Date 12/1/20
 **/
public class ContextHolder {

    public static ApplicationContext appContext;

    public static <T> Collection<T> getBeansOfType(Class<T> clazz) {
        return appContext.getBeansOfType(clazz).values();
    }

    public static <T> T getBean(Class<T> clazz) {
        return appContext.getBean(clazz);
    }
}
