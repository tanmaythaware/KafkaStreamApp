package com.example.streamappdummy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.core.env.Environment;


public class Utils {
    private static ObjectMapper objectMapper;
    private static Environment environment;

    Utils() {
        // Utils should not have public constructors
    }

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.setConfig(
                    objectMapper.getSerializationConfig().without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        }
        return objectMapper;
    }

    public static Environment getEnvironment() {
        if (environment == null) {
            environment = BeanUtil.getBean(Environment.class);
        }
        return environment;
    }
}
