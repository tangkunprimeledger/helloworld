package com.higgs.trust.slave.core.service.datahandler.manage;

import com.higgs.trust.slave.model.bo.manage.CancelRS;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsNode;

/**
 * @author tangfashuang
 * @date 2018/04/17 19:23
 * @desc rs handler interface
 */
public interface RsHandler {
    /**
     * get RsNode
     * @param rsId
     * @return
     */
    RsNode getRsNode(String rsId);

    /**
     * register RsNode
     * @param registerRS
     */
    void registerRsNode(RegisterRS registerRS);

    /**
     * cancel rs
     * @param cancelRS
     */
    void cancelRsNode(CancelRS cancelRS);
}
