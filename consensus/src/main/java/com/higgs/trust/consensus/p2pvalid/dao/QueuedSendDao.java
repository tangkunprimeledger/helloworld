package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedSendPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QueuedSendDao extends BaseDao<QueuedSendPO> {
    public QueuedSendPO queryFirst();
}
