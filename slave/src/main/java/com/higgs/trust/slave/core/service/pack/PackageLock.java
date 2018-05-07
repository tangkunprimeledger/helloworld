package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.service.consensus.log.LogReplicateHandler;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
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
@Service
@Slf4j
public class PackageLock {
    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private TransactionTemplate txNested;

    @Autowired
    private PackageService packageService;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private LogReplicateHandler logReplicateHandler;

    /**
     * lock and submit package to consensus layer
     *
     * @param height
     */
    public void lockAndSubmit(Long height) {

        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Package pack = packageRepository.loadAndLock(height);

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
                String sign = packageService.getSign(packageVO);
                if (StringUtils.isEmpty(sign)) {
                    log.error("get signature failed.");
                    //TODO 添加告警
                    return;
                }
                packageVO.setSign(sign);

                //send package to log replicate consensus layer
                logReplicateHandler.replicatePackage(packageVO);
                // update status
                packageService.statusChange(pack, PackageStatusEnum.INIT, PackageStatusEnum.SUBMIT_CONSENSUS_SUCCESS);
            }
        });
    }

    /**
     * lock and validating package
     *
     * @param height
     */
    public void lockAndValidating(Long height) {
        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Package pack = packageRepository.loadAndLock(height);

                if (null == pack) {
                    log.error("system exception, package is empty, height={}", height);
                    //TODO 添加告警
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_IS_NOT_EXIST);
                }

                // if package status is not 'RECEIVED', return directly.
                if (PackageStatusEnum.RECEIVED != pack.getStatus()) {
                    return;
                }

                if (Constant.GENESIS_HEIGHT != height - 1) {
                    Package lastPack = packageRepository.load(height - 1);
                    if (PackageStatusEnum.PERSISTED != lastPack.getStatus()) {
                        log.info("last package is not persisted, waiting!");
                        throw new SlaveException(SlaveErrorEnum.SLAVE_LAST_PACKAGE_NOT_FINISH);
                    }
                }

                // check next package height
                if (!pack.getHeight().equals(blockRepository.getMaxHeight() + 1)) {
                    log.warn("package.height: {} is unequal db.height:{}", pack.getHeight(), blockRepository.getMaxHeight() + 1);
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT);
                }

                PackContext packContext = packageService.createPackContext(pack);
                // do validate
                packageService.validating(packContext);
                // update status
                packageService.statusChange(pack, PackageStatusEnum.RECEIVED, PackageStatusEnum.VALIDATING);
            }
        });
    }

    /**
     * lock and send validated results to consensus layer
     *
     * @param height
     */
    public void lockValidatingAndSubmit(Long height) {
        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Package pack = packageRepository.loadAndLock(height);

                if (null == pack) {
                    log.error("system exception, package is empty, height={}", height);
                    //TODO 添加告警
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_IS_NOT_EXIST);
                }

                // if package status is not 'VALIDATING', return directly.
                if (PackageStatusEnum.VALIDATING != pack.getStatus()) {
                    return;
                }
                packageService.validateConsensus(pack);
                // update status
                packageService.statusChange(pack, PackageStatusEnum.VALIDATING, PackageStatusEnum.WAIT_VALIDATE_CONSENSUS);
            }
        });
    }

    /**
     * lock and do something after validating
     *
     * @param height
     */
    public void lockAndValidated(Long height) {
        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Package pack = packageRepository.loadAndLock(height);

                if (null == pack) {
                    log.error("system exception, package is empty, height={}", height);
                    //TODO 添加告警
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_IS_NOT_EXIST);
                }
                // if package status is not 'WAIT_VALIDATE_CONSENSUS', return directly.
                if (PackageStatusEnum.WAIT_VALIDATE_CONSENSUS != pack.getStatus()) {
                    return;
                }

                packageService.validated(pack);
                // update status
                packageService.statusChange(pack, PackageStatusEnum.WAIT_VALIDATE_CONSENSUS, PackageStatusEnum.VALIDATED);

            }
        });
    }


    /**
     * lock and persisting package
     *
     * @param height
     */
    public void lockAndPersist(Long height) {
        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Package pack = packageRepository.loadAndLock(height);

                if (null == pack) {
                    log.error("system exception, package is empty, height={}", height);
                    //TODO 添加告警
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_IS_NOT_EXIST);
                }

                // if package status is not 'VALIDATED', return directly.
                if (PackageStatusEnum.VALIDATED != pack.getStatus()) {
                    return;
                }

                // check next package height
                if (!pack.getHeight().equals(blockRepository.getMaxHeight() + 1)) {
                    log.warn("package.height: {} is unequal db.height:{}", pack.getHeight(), blockRepository.getMaxHeight() + 1);
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT);
                }

                PackContext packContext = packageService.createPackContext(pack);
                // do persist
                packageService.persisting(packContext);
                // update status
                packageService.statusChange(pack, PackageStatusEnum.VALIDATED, PackageStatusEnum.PERSISTING);
            }
        });
    }

    /**
     * lock and send persisting  results to  consensus layer
     *
     * @param height
     */
    public void lockPersistingAndSubmit(Long height) {
        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Package pack = packageRepository.loadAndLock(height);

                if (null == pack) {
                    log.error("system exception, package is empty, height={}", height);
                    //TODO 添加告警
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_IS_NOT_EXIST);
                }

                // if package status is not 'PERSISTING', return directly.
                if (PackageStatusEnum.PERSISTING != pack.getStatus()) {
                    return;
                }

                // check package height
                //TODO lingchao  改造 支持不等待persist 共识。就进行下一个区块处理
                if (!pack.getHeight().equals(blockRepository.getMaxHeight())) {
                    log.warn("package.height: {} is unequal db.height:{}", pack.getHeight(), blockRepository.getMaxHeight());
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT);
                }

                packageService.persistConsensus(pack);
                // update status
                packageService.statusChange(pack, PackageStatusEnum.PERSISTING, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);

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
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Package pack = packageRepository.loadAndLock(height);

                if (null == pack) {
                    log.error("system exception, package is empty, height={}", height);
                    //TODO 添加告警
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_IS_NOT_EXIST);
                }

                // if package status is not 'WAIT_PERSIST_CONSENSUS', return directly.
                if (PackageStatusEnum.WAIT_PERSIST_CONSENSUS != pack.getStatus()) {
                    return;
                }

                // check package height
                //TODO lingchao  改造 支持不等待persist 共识。就进行下一个区块处理
                if (!pack.getHeight().equals(blockRepository.getMaxHeight())) {
                    log.warn("package.height: {} is unequal db.height:{}", pack.getHeight(), blockRepository.getMaxHeight());
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT);
                }

                packageService.persisted(pack);
                // update status
                packageService.statusChange(pack, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
            }
        });
    }
}
