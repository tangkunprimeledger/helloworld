package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.SendCommandPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SendCommandDao extends BaseDao<SendCommandPO> {
    public SendCommandPO queryByMessageDigest(@Param("messageDigest") String messageDigest);
    public void transStatus(@Param("messageDigest") String messageDigest, @Param("status") Integer status);
    public void updateAckNodeNum(@Param("messageDigest") String messageDigest, @Param("ackNodeNum") Integer ackNodeNum);
}
