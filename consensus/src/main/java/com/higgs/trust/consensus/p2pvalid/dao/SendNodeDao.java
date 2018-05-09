package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.SendNodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SendNodeDao extends BaseConsensusDao<SendNodePO> {
    public List<SendNodePO> queryByDigestAndStatus(@Param("messageDigest") String messageDigest, @Param("status") Integer status);
    public void transStatus(@Param("messageDigest") String messageDigest, @Param("status") Integer status);
}
