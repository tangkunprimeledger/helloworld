package com.higgs.trust.rs.core.repository;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.core.api.enums.VoteResultEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.bo.VoteRequestRecord;
import com.higgs.trust.rs.core.dao.VoteRequestRecordDao;
import com.higgs.trust.rs.core.dao.po.VoteRequestRecordPO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
@Slf4j @Repository public class VoteReqRecordRepository {
    @Autowired private RsConfig rsConfig;
    @Autowired private VoteRequestRecordDao voteRequestRecordDao;

    /**
     * create new vote-request-record
     *
     * @param voteRequestRecord
     */
    public void add(VoteRequestRecord voteRequestRecord) {
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return;
        }
        VoteRequestRecordPO voteRequestRecordPO =
            BeanConvertor.convertBean(voteRequestRecord, VoteRequestRecordPO.class);
        //default INIT result
        voteRequestRecordPO.setVoteResult(VoteResultEnum.INIT.getCode());
        voteRequestRecordPO.setCreateTime(new Date());
        try {
            voteRequestRecordDao.add(voteRequestRecordPO);
        } catch (DuplicateKeyException e) {
            log.error("[add.vote-request-record] is idempotent by txId:{}", voteRequestRecord.getTxId());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
    }

    /**
     * query vote-request-record by transaction id
     *
     * @param txId
     * @return
     */
    public VoteRequestRecord queryByTxId(String txId) {
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return null;
        }
        VoteRequestRecordPO voteRequestRecordPO = voteRequestRecordDao.queryByTxId(txId);
        if (voteRequestRecordPO == null) {
            return null;
        }
        VoteRequestRecord voteRequestRecord = BeanConvertor.convertBean(voteRequestRecordPO, VoteRequestRecord.class);
        voteRequestRecord.setVoteResult(VoteResultEnum.fromCode(voteRequestRecordPO.getVoteResult()));
        return voteRequestRecord;
    }

    /**
     * query vote-request-record by transaction id
     *
     * @param txId
     * @param sign
     * @param voteResult
     * @return
     */
    public void setVoteResult(String txId, String sign,VoteResultEnum voteResult) {
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return;
        }
        int r = voteRequestRecordDao.setVoteResult(txId, sign,voteResult.getCode());
        if (r != 1) {
            log.error("[setVoteResult] is fail by txId:{}", txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_VOTE_SET_RESULT_ERROR);
        }
    }

    /**
     * query all request for init result
     *
     * @param row
     * @param count
     * @return
     */
    public List<VoteRequestRecord> queryAllInitRequest(int row,int count){
        List<VoteRequestRecordPO> poList = voteRequestRecordDao.queryAllInitRequest(row,count);
        if(CollectionUtils.isEmpty(poList)){
            return null;
        }
        List<VoteRequestRecord> list = new ArrayList<>();
        for(VoteRequestRecordPO po : poList){
            VoteRequestRecord requestRecord = BeanConvertor.convertBean(po,VoteRequestRecord.class);
            requestRecord.setVoteResult(VoteResultEnum.fromCode(po.getVoteResult()));
            list.add(requestRecord);
        }
        return list;
    }
}
