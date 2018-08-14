package com.higgs.trust.slave.common.context;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.util.asynctosync.HashBlockingMap;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.common.constant.Constant;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class AppContext implements ApplicationContextAware {
    /**
     * spring context
     */
    public static ApplicationContext springContext;

    public static HashBlockingMap<RespData> TX_HANDLE_RESULT_MAP = new HashBlockingMap<>(Constant.MAX_BLOCKING_QUEUE_SIZE);

    public static ConcurrentLinkedQueue<SignedTransaction> PENDING_TO_SUBMIT_QUEUE = new ConcurrentLinkedQueue<>();

    @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        springContext = applicationContext;
    }
}
