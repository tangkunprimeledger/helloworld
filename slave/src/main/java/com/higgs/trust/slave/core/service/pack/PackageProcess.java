package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author tangfashuang
 * @date 2018/04/17 11:43
 */
@Service @Slf4j public class PackageProcess {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private PackageService packageService;

    @Autowired
    private TransactionTemplate txNested;

    /**
     * package process logic
     *
     * @param height
     */
    public void process(final Long height) {
        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                try {
                    Package pack = packageRepository.loadAndLock(height);
                    if (null == pack) {
                        log.error("cannot acquire package, invalid height[{}].", height);
                        return;
                    }

                    packageFSM(pack);
                } catch (SlaveException e) {
                    if (SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR == e.getCode()
                        || SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT == e.getCode()
                        || SlaveErrorEnum.SLAVE_LAST_PACKAGE_NOT_FINISH == e.getCode()) {
                        throw e;
                    }
                    log.error("slave exception. ", e);
                    throw e;
                } catch (Throwable e) {
                    if (e instanceof CannotAcquireLockException) {
                        log.warn("cannot acquire package lock, height={}", height);
                    } else {
                        log.error("package process exception. ", e);
                        //terminal recycle
                        throw e;
                    }
                }
            }
        });
    }

    public void process(final Package pack) {
        process(pack.getHeight());
    }

    private void packageFSM(Package pack) {
        //package status equals 'WAIT_VALIDATE_CONSENSUS' or 'WAIT_PERSIST_CONSENSUS' or 'PERSISTED' will jump out recycle
        while ((PackageStatusEnum.PERSISTED != pack.getStatus())) {
            switch (pack.getStatus()) {
                case RECEIVED:
                    doValidate(pack);
                    pack.setStatus(PackageStatusEnum.VALIDATING);
                    break;
                case VALIDATING:
                    doValidatingToConsensus(pack);
                    //no need for update pack status
                    pack.setStatus(PackageStatusEnum.WAIT_VALIDATE_CONSENSUS);
                    return;
                case WAIT_VALIDATE_CONSENSUS:
                    doValidated(pack);
                    pack.setStatus(PackageStatusEnum.VALIDATED);
                    break;
                case VALIDATED:
                    doPersist(pack);
                    pack.setStatus(PackageStatusEnum.PERSISTING);
                    break;
                case PERSISTING:
                    doPersistingToConsensus(pack);
                    // no need update package status
                    pack.setStatus(PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
                    return;
                case WAIT_PERSIST_CONSENSUS:
                    doPersisted(pack);
                    pack.setStatus(PackageStatusEnum.PERSISTED);
                    break;
                case PERSISTED:
                    //finish
                    break;
                default:
                    log.error("package status is invalid, status={}", pack.getStatus());
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_NO_SUCH_STATUS);
            }

        }
    }

    private void doValidate(Package pack) {
        // if package status is not 'RECEIVED', return directly.
        if (PackageStatusEnum.RECEIVED != pack.getStatus()) {
            return;
        }

        Long height = pack.getHeight();
        if (Constant.GENESIS_HEIGHT != height - 1) {
            Package lastPack = packageRepository.load(height - 1);
            if (PackageStatusEnum.PERSISTED != lastPack.getStatus()) {
                log.info("last package is not persisted, waiting!");
                throw new SlaveException(SlaveErrorEnum.SLAVE_LAST_PACKAGE_NOT_FINISH);
            }
        }

        // check next package height
        Long maxBlockHeight = blockRepository.getMaxHeight();
        if (!pack.getHeight().equals(maxBlockHeight + 1)) {
            log.warn("package.height: {} is unequal db.height:{}", pack.getHeight(), maxBlockHeight + 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT);
        }

        PackContext packContext = packageService.createPackContext(pack);
        // do validate
        packageService.validating(packContext);
    }


    private void doValidatingToConsensus(Package pack) {
        // if package status is not 'VALIDATING', return directly.
        if (PackageStatusEnum.VALIDATING != pack.getStatus()) {
            return;
        }
        packageService.validateConsensus(pack);
        // update status
        packageService.statusChange(pack, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_VALIDATE_CONSENSUS);
    }


    private void doValidated(Package pack) {
        // if package status is not 'WAIT_VALIDATE_CONSENSUS', return directly.
        if (PackageStatusEnum.WAIT_VALIDATE_CONSENSUS != pack.getStatus()) {
            return;
        }
        packageService.validated(pack);
    }


    private void doPersist(Package pack) {
        // if package status is not 'VALIDATED', return directly.
        if (PackageStatusEnum.VALIDATED != pack.getStatus()) {
            return;
        }

        // check next package height
        if (!pack.getHeight().equals(blockRepository.getMaxHeight() + 1)) {
            log.warn("package.height: {} is unequal db.height:{}", pack.getHeight(),
                blockRepository.getMaxHeight() + 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT);
        }

        PackContext packContext = packageService.createPackContext(pack);
        // do persist
        packageService.persisting(packContext);
    }


    private void doPersistingToConsensus(Package pack) {
        // if package status is not 'PERSISTING', return directly.
        if (PackageStatusEnum.PERSISTING != pack.getStatus()) {
            return;
        }

        // check package height
        if (!pack.getHeight().equals(blockRepository.getMaxHeight())) {
            log.warn("package.height: {} is unequal db.height:{}", pack.getHeight(),
                blockRepository.getMaxHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT);
        }

        packageService.persistConsensus(pack);
        // update status
        packageService.statusChange(pack, PackageStatusEnum.WAIT_VALIDATE_CONSENSUS, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
    }


    private void doPersisted(Package pack) {
        // if package status is not 'WAIT_PERSIST_CONSENSUS', return directly.
        if (PackageStatusEnum.WAIT_PERSIST_CONSENSUS != pack.getStatus()) {
            return;
        }

        // check package height
        if (!pack.getHeight().equals(blockRepository.getMaxHeight())) {
            log.warn("package.height: {} is unequal db.height:{}", pack.getHeight(),
                blockRepository.getMaxHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT);
        }

        packageService.persisted(pack);
        // update status
        packageService.statusChange(pack, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
    }
}
