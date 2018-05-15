package com.higgs.trust.rs.core.scheduler;

import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.dao.CoreTransactionDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service @Slf4j public class TxSubmitSlaveSchedule {
    @Autowired private CoreTransactionService coreTransactionService;
    @Autowired private CoreTransactionDao coreTransactionDao;

    @Scheduled(fixedDelayString = "${rs.core.schedule.submitSlave:500}") public void exe() {
        coreTransactionService.submitToSlave();
    }
}
