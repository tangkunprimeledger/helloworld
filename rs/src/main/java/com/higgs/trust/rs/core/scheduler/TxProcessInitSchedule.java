package com.higgs.trust.rs.core.scheduler;

import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import com.higgs.trust.rs.core.repository.CoreTxProcessRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@ConditionalOnProperty(name = "higgs.trust.joinConsensus", havingValue = "true", matchIfMissing = true)
@Service
@Slf4j
public class TxProcessInitSchedule {
    @Autowired
    private CoreTransactionService coreTransactionService;
    @Autowired
    private CoreTxProcessRepository coreTxProcessRepository;

    private int pageNo = 1;
    private int pageSize = 200;
    private int maxPageNo = 1000;
    /**
     * rocks db seek key:01-tx_id
     */
    private String lastPreKey = null;

    @Scheduled(fixedRateString = "${rs.core.schedule.processInit:500}")
    public void exe() {
        List<CoreTransactionProcessPO> list = coreTxProcessRepository.queryByStatus(CoreTxStatusEnum.INIT, (pageNo - 1) * pageSize, pageSize,lastPreKey);
        if (CollectionUtils.isEmpty(list) || pageNo == maxPageNo) {
            pageNo = 1;
            lastPreKey = null;
            return;
        }
        int size = list.size();
        //TODO:for press test
        log.info("process init.size:{}",size);
        list.forEach(entry->{
            try {
                coreTransactionService.processInitTx(entry.getTxId());
            } catch (Throwable e) {
                log.error("has error", e);
            }
        });
//        lastPreKey = list.get(size - 1).getTxId();
        pageNo++;
    }
}
