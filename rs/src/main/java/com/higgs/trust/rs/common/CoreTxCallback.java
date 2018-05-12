package com.higgs.trust.rs.common;

import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.vo.CoreTxVO;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public interface CoreTxCallback {
    /**
     * on slave persisted phase,only current node persisted
     *
     * @param bizTypeEnum
     * @param coreTxVO
     */
    void onPersisted(BizTypeEnum bizTypeEnum,CoreTxVO coreTxVO);

    /**
     * on slave end phase,cluster node persisted
     *
     * @param bizTypeEnum
     * @param coreTxVO
     */
    void onEnd(BizTypeEnum bizTypeEnum,CoreTxVO coreTxVO);
}
