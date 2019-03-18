package com.higgs.trust.network.springboot;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.PriorityOrdered;

/**
 * @author duhongming
 * @date 2018/9/11
 */
public class SpringContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>,PriorityOrdered {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        System.out.println("SpringContextInitializer ...");
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
