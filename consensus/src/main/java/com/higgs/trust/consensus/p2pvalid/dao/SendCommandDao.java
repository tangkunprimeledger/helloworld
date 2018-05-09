package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.SendCommandPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SendCommandDao extends BaseConsensusDao<SendCommandPO> {
    SendCommandPO queryByMessageDigest(@Param("messageDigest") String messageDigest);
    void transStatus(@Param("messageDigest") String messageDigest, @Param("status") Integer status);
    void updateAckNodeNum(@Param("messageDigest") String messageDigest, @Param("ackNodeNum") Integer ackNodeNum);
}
