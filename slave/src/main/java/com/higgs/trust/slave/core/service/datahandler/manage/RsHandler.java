package com.higgs.trust.slave.core.service.datahandler.manage;

import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;

/**
 * @author tangfashuang
 * @date 2018/04/17 19:23
 * @desc rs handler interface
 */
public interface RsHandler {
    /**
     * get RsPubKey
     * @param rsId
     * @return
     */
    RsPubKey getRsPubKey(String rsId);

    /**
     * register RsPubKey
     * @param registerRS
     */
    void registerRsPubKey(RegisterRS registerRS);
}
