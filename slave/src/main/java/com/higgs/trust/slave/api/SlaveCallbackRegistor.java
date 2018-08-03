package com.higgs.trust.slave.api;

import org.springframework.stereotype.Repository;

/**
 * @author liuyu
 * @description
 * @date 2018-05-13
 */
@Repository public class SlaveCallbackRegistor {
    private SlaveCallbackHandler slaveCallbackHandler;
    private SlaveBatchCallbackHandler slaveBatchCallbackHandler;

    public void registCallbackHandler(SlaveCallbackHandler callbackHandler) {
        this.slaveCallbackHandler = callbackHandler;
    }

    public void registBatchCallbackHandler(SlaveBatchCallbackHandler callbackHandler) {
        this.slaveBatchCallbackHandler = callbackHandler;
    }

    public SlaveCallbackHandler getSlaveCallbackHandler() {
        return slaveCallbackHandler;
    }

    public SlaveBatchCallbackHandler getSlaveBatchCallbackHandler() {
        return slaveBatchCallbackHandler;
    }
}
