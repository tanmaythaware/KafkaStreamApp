package com.example.streamappdummy.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BeanUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    private static void setContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * provides required Bean
     *
     * @param beanClass class of required bean
     * @return required bean
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        log.trace("Setting app context for local use");
        setContext(applicationContext);
    }
}
