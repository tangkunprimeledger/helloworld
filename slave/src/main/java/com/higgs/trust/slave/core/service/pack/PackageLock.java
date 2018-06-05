package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.service.consensus.log.LogReplicateHandler;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.convert.PackageConvert;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author tangfashuang
 * @date 2018/04/13 11:19
 * @desc package lock
 */
@Service @Slf4j public class PackageLock {
    @Autowired private PackageRepository packageRepository;

    @Autowired private TransactionTemplate txNested;

    @Autowired private PackageService packageService;

    @Autowired private LogReplicateHandler logReplicateHandler;

    public void lockAndSubmit(Long height) {

        Package pack = packageRepository.load(height);

        if (null == pack) {
            log.error("system exception, package is empty, height={}", height);
            //TODO 添加告警
            return;
        }

        // if package status is not 'INIT', return directly.
        if (PackageStatusEnum.INIT != pack.getStatus()) {
            return;
        }

        PackageVO packageVO = PackageConvert.convertPackToPackVO(pack);

        //send package to log replicate consensus layer
        logReplicateHandler.replicatePackage(packageVO);

    }

    /**
     * lock and send persisting  results to  consensus layer
     *
     * @param height
     */
    public void lockPersistingAndSubmit(Long height) {
        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Package pack = packageRepository.loadAndLock(height);

                if (null == pack) {
                    log.error("system exception, package is empty, height={}", height);
                    //TODO 添加告警
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_IS_NOT_EXIST);
                }

                // if package status is not 'PERSISTING', return directly.
                if (PackageStatusEnum.PERSISTING != pack.getStatus()) {
                    log.info("Persisting and submit do not get lock");
                    return;
                }
                packageService.persistConsensus(pack);
                // update status
                packageService
                    .statusChange(pack, PackageStatusEnum.PERSISTING, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);

            }
        });
    }

    /**
     * lock and do something after persisting
     *
     * @param height
     */
    public void lockAndPersisted(Long height) {
        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Package pack = packageRepository.loadAndLock(height);

                if (null == pack) {
                    log.error("system exception, package is empty, height={}", height);
                    //TODO 添加告警
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_IS_NOT_EXIST);
                }

                // if package status is not 'WAIT_PERSIST_CONSENSUS', return directly.
                if (PackageStatusEnum.WAIT_PERSIST_CONSENSUS != pack.getStatus()) {
                    log.info("Persisted  do not get lock");
                    return;
                }

                packageService.persisted(pack);
                // update status
                packageService
                    .statusChange(pack, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
            }
        });
    }
}
