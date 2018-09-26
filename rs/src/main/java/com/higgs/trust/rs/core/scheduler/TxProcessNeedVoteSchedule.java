package com.higgs.trust.rs.core.scheduler;

import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
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
    private CoreTxRepository coreTxRepository;
    @Autowired
    private RsConfig rsConfig;

    private int pageNo = 1;
    private int pageSize = 100;
    /**
     * rocks db seek key:01-tx_id
     */
    private String lastPreKey = null;

    @Scheduled(fixedDelayString = "${rs.core.schedule.processNeedVote:3000}")
    public void exe() {
        if (rsConfig.isUseMySQL()) {
            List<CoreTransactionProcessPO> list = coreTxRepository
                .queryByStatusFromMysql(CoreTxStatusEnum.NEED_VOTE, (pageNo - 1) * pageSize, pageSize, lastPreKey);
            if (CollectionUtils.isEmpty(list)) {
                pageNo = 1;
                lastPreKey = null;
                return;
            }
            for (CoreTransactionProcessPO po : list) {
                try {
                    coreTransactionService.processNeedVoteTx(po.getTxId());
                } catch (Throwable e) {
                    log.error("has error", e);
                }
            }
            lastPreKey = list.get(list.size() - 1).getTxId();
        } else {
            List<CoreTransactionPO> list = coreTxRepository
                .queryByStatusFromRocks(CoreTxStatusEnum.NEED_VOTE, (pageNo - 1) * pageSize, pageSize, lastPreKey);
            if (CollectionUtils.isEmpty(list)) {
                pageNo = 1;
                lastPreKey = null;
                return;
            }
            for (CoreTransactionPO po : list) {
                try {
                    coreTransactionService.processNeedVoteTx(po.getTxId());
                } catch (Throwable e) {
                    log.error("has error", e);
                }
            }
            lastPreKey = list.get(list.size() - 1).getTxId();
        }

        pageNo++;
    }
}
