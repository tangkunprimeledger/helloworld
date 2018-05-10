package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedApplyDelayPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QueuedApplyDelayDao extends BaseConsensusDao<QueuedApplyDelayPO> {
    List<QueuedApplyDelayPO> queryListByApplyTime(@Param("applyTime") Long applyTime);
}
