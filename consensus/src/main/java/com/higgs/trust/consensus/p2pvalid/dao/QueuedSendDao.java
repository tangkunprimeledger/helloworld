package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedSendPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QueuedSendDao extends BaseDao<QueuedSendPO> {
    public List<QueuedSendPO> querySendList();
    public void deleteByMessageDigestList(@Param("messageDigestList") List<String> messageDigestList);
}
