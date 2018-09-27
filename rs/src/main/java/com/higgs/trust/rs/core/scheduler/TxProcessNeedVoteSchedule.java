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

@ConditionalOnProperty(name = "higgs.trust.isSlave", havingValue = "true", matchIfMissing = true)
@Service
@Slf4j
public class TxProcessNeedVoteSchedule {
    @Autowired
    private CoreTransactionService coreTransactionService;
    @Autowired
    private CoreTxProcessRepository coreTxProcessRepository;

    private int pageNo = 1;
    private int pageSize = 100;

    @Scheduled(fixedDelayString = "${rs.core.schedule.processNeedVote:3000}")
    public void exe() {
        List<CoreTransactionProcessPO> list = coreTxProcessRepository.queryByStatus(CoreTxStatusEnum.NEED_VOTE, (pageNo - 1) * pageSize, pageSize);
        if (CollectionUtils.isEmpty(list)) {
            pageNo = 1;
            return;
        }
        for (CoreTransactionProcessPO po : list) {
            try {
                coreTransactionService.processNeedVoteTx(po.getTxId());
            } catch (Throwable e) {
                log.error("has error", e);
            }
        }
        pageNo++;
    }
}
