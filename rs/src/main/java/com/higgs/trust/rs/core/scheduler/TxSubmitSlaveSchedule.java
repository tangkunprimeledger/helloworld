package com.higgs.trust.rs.core.scheduler;

import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import com.higgs.trust.rs.core.repository.CoreTxProcessRepository;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@ConditionalOnProperty(name = "higgs.trust.joinConsensus", havingValue = "true", matchIfMissing = true) @Service @Slf4j
public class TxSubmitSlaveSchedule {
    @Autowired private CoreTransactionService coreTransactionService;
    @Autowired private CoreTxProcessRepository coreTxProcessRepository;
    @Autowired private CoreTxRepository coreTxRepository;
    private int pageNo = 1;
    private int pageSize = 1000;
    private int maxPageNo = 1000;
    /**
     * rocks db seek key:01-tx_id
     */
    private String lastPreKey = null;

    @Scheduled(fixedRateString = "${rs.core.schedule.submitSlave:500}") public void exe() {
        List<CoreTransactionProcessPO> list =
            coreTxProcessRepository.queryByStatus(CoreTxStatusEnum.WAIT, (pageNo - 1) * pageSize, pageSize, lastPreKey);
        if (CollectionUtils.isEmpty(list) || pageNo == maxPageNo) {
            pageNo = 1;
            lastPreKey = null;
            return;
        }
        int size = list.size();
        //TODO:for press test
        log.info("submit.size:{}",size);
        List<String> txIdList = new ArrayList<>(size);
        list.forEach(entry->{
            txIdList.add(entry.getTxId());
        });
        //reset preKey by last txId
//        lastPreKey = txIdList.get(size - 1);

        List<CoreTransactionPO> coreTransactionPOList = coreTxRepository.queryByTxIds(txIdList);
        List<CoreTxBO> boList = new ArrayList<>(size);
        coreTransactionPOList.forEach(entry->{
            boList.add(coreTxRepository.convertTxBO(entry));
        });
        coreTransactionService.submitToSlave(boList);
        pageNo++;
    }
}
