package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedSendGcPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QueuedSendGcDao extends BaseDao<QueuedSendGcPO> {
    public List<QueuedSendGcPO> queryGcList(@Param("gcTime") Long gcTime);
    public void deleteByMessageDigestList(@Param("messageDigestList") List<String> messageDigestList);
}
