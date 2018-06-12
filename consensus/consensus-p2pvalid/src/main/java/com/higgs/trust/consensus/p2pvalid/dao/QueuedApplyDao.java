package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedApplyPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QueuedApplyDao extends BaseConsensusDao<QueuedApplyPO>{
    List<QueuedApplyPO> queryApplyList();
}
