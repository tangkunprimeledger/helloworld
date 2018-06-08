package com.higgs.trust.rs.core.scheduler;

import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @Slf4j public class TxProcessNeedVoteSchedule {
    @Autowired private CoreTransactionService coreTransactionService;
    @Autowired private CoreTxRepository coreTxRepository;

    private int pageNo = 1;
    private int pageSize = 100;

    @Scheduled(fixedDelayString = "${rs.core.schedule.processNeedVote:3000}") public void exe() {
        List<CoreTransactionPO> list =
            coreTxRepository.queryByStatus(CoreTxStatusEnum.NEED_VOTE, (pageNo - 1) * pageSize, pageSize);
        if (CollectionUtils.isEmpty(list)) {
            pageNo = 1;
            return;
        }
        for (CoreTransactionPO po : list) {
            try {
                coreTransactionService.processNeedVoteTx(po.getTxId());
            } catch (Throwable e) {
                log.error("has error", e);
            }
        }
        pageNo++;
    }
}
