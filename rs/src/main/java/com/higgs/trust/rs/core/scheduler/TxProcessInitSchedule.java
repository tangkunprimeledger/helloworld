package com.higgs.trust.rs.core.scheduler;

import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.CoreTransactionDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @Slf4j public class TxProcessInitSchedule {
    @Autowired private CoreTransactionService coreTransactionService;
    @Autowired private CoreTransactionDao coreTransactionDao;

    private int pageNo = 1;
    private int pageSize = 100;

    @Scheduled(fixedDelayString = "${rs.core.schedule.processInit:500}") public void exe() {
        List<CoreTransactionPO> list =
            coreTransactionDao.queryByStatus(CoreTxStatusEnum.INIT.getCode(), (pageNo - 1) * pageSize, pageSize);
        if(CollectionUtils.isEmpty(list)){
            pageNo = 1;
            return;
        }
        for (CoreTransactionPO po : list) {
            try {
                coreTransactionService.processInitTx(po.getTxId());
            }catch (Throwable e){
                log.error("has error",e);
            }
        }
        pageNo++;
    }
}
