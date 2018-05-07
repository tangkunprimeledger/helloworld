package com.higgs.trust.slave.common.context;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.asynctosync.HashBlockingMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class AppContext implements ApplicationContextAware {
    /**
     * spring context
     */
    public static ApplicationContext springContext;

    public static HashBlockingMap<RespData> TX_HANDLE_RESULT_MAP = new HashBlockingMap<>();

    @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        springContext = applicationContext;
    }
}
