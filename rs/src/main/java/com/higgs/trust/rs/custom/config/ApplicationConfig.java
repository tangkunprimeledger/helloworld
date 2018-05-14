package com.higgs.trust.rs.custom.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;

public class ApplicationConfig {

    @Autowired
    private RsPropertiesConfig config;

    @Bean
    public Jackson2ObjectMapperFactoryBean objectMapper() {
        Jackson2ObjectMapperFactoryBean bean = new Jackson2ObjectMapperFactoryBean();
        bean.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return bean;
    }

}
