package com.higgs.trust.consensus.p2pvalid.dao;

import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedSendDelayPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QueuedSendDelayDao extends BaseConsensusDao<QueuedSendDelayPO> {
    public List<QueuedSendDelayPO> queryListBySendTime(@Param("sendTime") Long sendTime);
}
