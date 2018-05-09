package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedApplyPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QueuedApplyDao extends BaseDao<QueuedApplyPO> {
}
