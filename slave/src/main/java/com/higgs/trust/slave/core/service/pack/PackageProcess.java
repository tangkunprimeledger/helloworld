package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;

/**
 * @author tangfashuang
 * @date 2018/04/17 11:43
 */
@Service @Slf4j public class PackageProcess {

    @Autowired private PackageRepository packageRepository;

    @Autowired private PackageLock packageLock;

    /**
     * package process logic
     *
     * @param height
     */
    public void process(final Long height) {
        Package pack = packageRepository.load(height);
        if (null == pack) {
            log.error("cannot acquire package, invalid height[{}].", height);
            return;
        }
        try {
            packageFSM(pack);
        } catch (SlaveException e) {
            if (SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR != e.getCode()) {
                log.info("slave exception. ", e);
            }
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

    public void process(final Package pack) {
        process(pack.getHeight());
    }

    private void packageFSM(Package pack) {
        //package status equals 'WAIT_VALIDATE_CONSENSUS' or 'WAIT_PERSIST_CONSENSUS' or 'PERSISTED' will jump out recycle
        while ((PackageStatusEnum.PERSISTED != pack.getStatus())) {

            switch (pack.getStatus()) {
                case RECEIVED:
                    doValidate(pack);
                    break;
                case VALIDATING:
                    doValidatingToConsensus(pack);
                    break;
                case WAIT_VALIDATE_CONSENSUS:
                    doValidated(pack);
                    break;
                case VALIDATED:
                    doPersist(pack);
                    break;
                case PERSISTING:
                    doPersistingToConsensus(pack);
                    break;
                case WAIT_PERSIST_CONSENSUS:
                    doPersisted(pack);
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
        packageLock.lockAndValidating(pack.getHeight());
        pack.setStatus(PackageStatusEnum.VALIDATING);
    }

    private void doValidatingToConsensus(Package pack) {
        packageLock.lockValidatingAndSubmit(pack.getHeight());
        pack.setStatus(PackageStatusEnum.WAIT_VALIDATE_CONSENSUS);
    }

    private void doValidated(Package pack) {
        packageLock.lockAndValidated(pack.getHeight());
        pack.setStatus(PackageStatusEnum.VALIDATED);
    }

    private void doPersist(Package pack) {
        packageLock.lockAndPersist(pack.getHeight());
        pack.setStatus(PackageStatusEnum.PERSISTING);
    }

    private void doPersistingToConsensus(Package pack) {
        packageLock.lockPersistingAndSubmit(pack.getHeight());
        pack.setStatus(PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
    }

    private void doPersisted(Package pack) {
        packageLock.lockAndPersisted(pack.getHeight());
        pack.setStatus(PackageStatusEnum.PERSISTED);
    }
}
