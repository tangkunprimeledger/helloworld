package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedSendDelayPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QueuedSendDelayDao extends BaseDao<QueuedSendDelayPO> {
}
