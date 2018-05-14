package com.higgs.trust.rs.core.scheduler;

import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.CoreTransactionDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.FailoverExecption;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.failover.BlockSyncService;
import com.higgs.trust.slave.core.service.failover.FailoverProperties;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;

@Service @Slf4j public class TxProcessInitSchedule {
    @Autowired private CoreTransactionService coreTransactionService;
    @Autowired private CoreTransactionDao coreTransactionDao;

    private int pageNo = 1;
    private int pageSize = 100;

    @Scheduled(fixedDelayString = "${rs.core.schedule.processInit:50}") public void exe() {
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
