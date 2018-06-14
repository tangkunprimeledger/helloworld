package com.higgs.trust.rs.core.repository;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.core.api.enums.VoteResultEnum;
import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.dao.VoteReceiptDao;
import com.higgs.trust.rs.core.dao.po.VoteReceiptPO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
@Slf4j @Repository public class VoteReceiptRepository {
    @Autowired private RsConfig rsConfig;
    @Autowired private VoteReceiptDao voteReceiptDao;

    /**
     * create new vote-receipt
     *
     * @param voteReceipt
     */
    public void add(VoteReceipt voteReceipt) {
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return;
        }
        VoteReceiptPO voteReceiptPO = BeanConvertor.convertBean(voteReceipt, VoteReceiptPO.class);
        voteReceiptPO.setVoteResult(voteReceipt.getVoteResult().getCode());
        voteReceiptPO.setCreateTime(new Date());
        try {
            voteReceiptDao.add(voteReceiptPO);
        } catch (DuplicateKeyException e) {
            log.error("[add.vote-receipt] is idempotent by txId:{}", voteReceiptPO.getTxId());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
    }

    /**
     * batch create new vote-receipt
     *
     * @param voteReceipts
     */
    public void batchAdd(List<VoteReceipt> voteReceipts) {
        if(CollectionUtils.isEmpty(voteReceipts)){
            return;
        }
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return;
        }
        List<VoteReceiptPO> list = new ArrayList<>(voteReceipts.size());
        for(VoteReceipt voteReceipt : voteReceipts){
            VoteReceiptPO po = new VoteReceiptPO();
            po.setTxId(voteReceipt.getTxId());
            po.setVoter(voteReceipt.getVoter());
            po.setSign(voteReceipt.getSign());
            po.setVoteResult(voteReceipt.getVoteResult().getCode());
            po.setCreateTime(new Date());
            list.add(po);
        }
        try {
            voteReceiptDao.batchAdd(list);
        } catch (DuplicateKeyException e) {
            log.error("[add.vote-receipt] is idempotent ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
    }

    /**
     * query vote-receipts by transaction id
     *
     * @param txId
     * @return
     */
    public List<VoteReceipt> queryByTxId(String txId) {
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return null;
        }
        List<VoteReceiptPO> list = voteReceiptDao.queryByTxId(txId);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<VoteReceipt> voteReceipts = new ArrayList<>(list.size());
        for (VoteReceiptPO po : list) {
            VoteReceipt voteReceipt = BeanConvertor.convertBean(po, VoteReceipt.class);
            voteReceipt.setVoteResult(VoteResultEnum.fromCode(po.getVoteResult()));
            voteReceipts.add(voteReceipt);
        }
        return voteReceipts;
    }

    /**
     * query vote-receipt by transaction-id and voter rs-name
     *
     * @param txId
     * @return
     */
    public VoteReceipt queryForVoter(String txId, String voter) {
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return null;
        }
        VoteReceiptPO po = voteReceiptDao.queryForVoter(txId, voter);
        if (po == null) {
            return null;
        }
        VoteReceipt voteReceipt = BeanConvertor.convertBean(po, VoteReceipt.class);
        voteReceipt.setVoteResult(VoteResultEnum.fromCode(po.getVoteResult()));
        return voteReceipt;
    }
}
