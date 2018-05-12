package com.higgs.trust.rs.common;

import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.vo.CoreTxVO;
import com.higgs.trust.slave.api.vo.RespData;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public interface TxCallbackHandler {
    /**
     * on slave persisted phase,only current node persisted
     *
     * @param bizTypeEnum
     * @param respData
     */
    void onPersisted(BizTypeEnum bizTypeEnum,RespData<CoreTxVO> respData);

    /**
     * on slave end phase,cluster node persisted
     *
     * @param bizTypeEnum
     * @param respData
     */
    void onEnd(BizTypeEnum bizTypeEnum,RespData<CoreTxVO> respData);
}
