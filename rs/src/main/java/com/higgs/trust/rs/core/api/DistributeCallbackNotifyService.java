package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.core.api.enums.RedisMegGroupEnum;
import com.higgs.trust.slave.api.vo.RespData;

import java.util.concurrent.TimeUnit;

/**
 * distribute callback notify
 * @author lingchao
 * @create 2018年08月23日14:27
 */
public interface DistributeCallbackNotifyService {
    /**
     * notify callback finish
     * @param txId
     * @param respData
     * @param redisMegGroupEnum
     */
     void notifySyncResult(String txId, RespData respData, RedisMegGroupEnum redisMegGroupEnum);

    /**
     * sync wait RespData
     * @param key
     * @param redisMegGroupEnum
     * @param timeout
     * @param timeUnit
     * @return
     */
     RespData syncWaitNotify(String key, RedisMegGroupEnum redisMegGroupEnum, long timeout, TimeUnit timeUnit);
}
