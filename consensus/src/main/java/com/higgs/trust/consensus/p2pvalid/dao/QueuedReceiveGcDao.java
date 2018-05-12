package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedReceiveGcPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QueuedReceiveGcDao extends BaseConsensusDao<QueuedReceiveGcPO> {
    List<QueuedReceiveGcPO> queryGcList(@Param("gcTime") Long gcTime);
}
