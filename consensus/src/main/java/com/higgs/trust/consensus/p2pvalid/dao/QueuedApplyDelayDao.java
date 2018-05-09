package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedApplyDelayPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QueuedApplyDelayDao extends BaseDao<QueuedApplyDelayPO> {
}
