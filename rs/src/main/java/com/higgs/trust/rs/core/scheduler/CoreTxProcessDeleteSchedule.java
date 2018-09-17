package com.higgs.trust.rs.core.scheduler;

import com.higgs.trust.rs.core.repository.CoreTxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * coreTxProcessDelete
 *
 * @author lingchao
 * @create 2018年08月22日16:32
 */

@Service
@Slf4j
@ConditionalOnProperty(name = "higgs.trust.joinConsensus", havingValue = "true", matchIfMissing = true)
public class CoreTxProcessDeleteSchedule {
    @Autowired
    private CoreTxRepository coreTxRepository;

    /**
     * task to delete coreTxProcess rows for status = END,
     * Each  one hour delete all status with END
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60) public void deleteEndRows() {
        log.info("Task to delete coreTransactionProcess for status with END");
       coreTxRepository.deleteEnd();
    }
}
