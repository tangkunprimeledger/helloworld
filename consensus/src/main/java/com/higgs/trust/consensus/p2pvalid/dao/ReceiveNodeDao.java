package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.ReceiveNodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReceiveNodeDao extends BaseConsensusDao<ReceiveNodePO> {
    ReceiveNodePO queryByMessageDigestAndFromNode(@Param("messageDigest") String messageDigest, @Param("fromNode") String fromNode);
}
