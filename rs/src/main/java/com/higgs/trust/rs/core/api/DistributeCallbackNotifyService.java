package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.core.api.enums.RedisMegGroupEnum;
import com.higgs.trust.slave.api.vo.RespData;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * distribute callback notify
 *
 * @author lingchao
 * @create 2018年08月23日14:27
 */
public interface DistributeCallbackNotifyService {
    /**
     * notify callback finish
     *
     * @param respDatas
     * @param redisMegGroupEnum
     */
    void notifySyncResult(List<RespData<String>> respDatas, RedisMegGroupEnum redisMegGroupEnum);

    /**
     * sync wait RespData
     *
     * @param key
     * @param redisMegGroupEnum
     * @param timeout
     * @param timeUnit
     * @return
     */
    RespData syncWaitNotify(String key, RedisMegGroupEnum redisMegGroupEnum, long timeout, TimeUnit timeUnit);
}
