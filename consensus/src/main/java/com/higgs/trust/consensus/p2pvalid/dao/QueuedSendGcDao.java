package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedSendGcPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QueuedSendGcDao extends BaseDao<QueuedSendGcPO> {
}
