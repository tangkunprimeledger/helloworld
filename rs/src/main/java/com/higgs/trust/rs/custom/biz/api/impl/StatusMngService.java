package com.higgs.trust.rs.custom.biz.api.impl;

import com.higgs.trust.rs.custom.api.StatefulService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author lingchao
 */
@Service
public class StatusMngService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusMngService.class);

    @Autowired
    private ApplicationContext applicationContext;

    public void init() {
        LOGGER.info("有状态服务 [Starting]");
        // 启动状态服务
        for (StatefulService statefulService : applicationContext.getBeansOfType(StatefulService.class).values()) {
            statefulService.init();
        }
        LOGGER.info("有状态服务 [OK]");
    }

}
