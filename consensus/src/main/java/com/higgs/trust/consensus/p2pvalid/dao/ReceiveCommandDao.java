package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.ReceiveCommandPO;
import com.higgs.trust.consensus.p2pvalid.dao.po.SendCommandPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReceiveCommandDao extends BaseConsensusDao<ReceiveCommandPO> {
    ReceiveCommandPO queryByMessageDigest(@Param("messageDigest") String messageDigest);
    int transStatus(@Param("messageDigest") String messageDigest, @Param("from") Integer from, @Param("status") Integer status);
    int increaseReceiveNodeNum(@Param("messageDigest") String messageDigest);
    int updateCloseStatus(@Param("messageDigest")String messageDigest, @Param("from") Integer from, @Param("closed") Integer closed);
}
