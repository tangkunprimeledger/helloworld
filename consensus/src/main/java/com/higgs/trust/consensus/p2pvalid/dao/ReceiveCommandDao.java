package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.ReceiveCommandPO;
import com.higgs.trust.consensus.p2pvalid.dao.po.SendCommandPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReceiveCommandDao extends BaseConsensusDao<ReceiveCommandPO> {
    ReceiveCommandPO queryByMessageDigest(@Param("messageDigest") String messageDigest);
    void transStatus(@Param("messageDigest") String messageDigest, @Param("status") Integer status);
    void updateReceiveNodeNum(@Param("messageDigest") String messageDigest, @Param("receiveNodeNum") Integer receiveNodeNum);
}
