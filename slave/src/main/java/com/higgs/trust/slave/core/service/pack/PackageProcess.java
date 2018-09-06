package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tangfashuang
 * @date 2018/04/17 11:43
 */
@Service @Slf4j public class PackageProcess implements InitializingBean {

    @Autowired private PackageRepository packageRepository;

    @Autowired private BlockRepository blockRepository;

    @Autowired private PackageService packageService;

    @Autowired private TransactionTemplate txNested;
    /**
     * current processed block height
     */
    private AtomicLong processedHeight = new AtomicLong(0);

    /**
     * initializing
     *
     * @throws Exception
     */
    @Override public void afterPropertiesSet() throws Exception {
        initProcessedHeight();
    }

    /**
     * package process logic
     *
     * @param height
     */
    public void process(final Long height) {
        boolean result = txNested.execute(transactionStatus -> {
            try {
                Package pack = packageRepository.loadAndLock(height);
                if (null == pack) {
                    log.error("cannot acquire package, invalid height[{}].", height);
                    return false;
                }
                //process only deal the package with status RECEIVED in DB
                if (pack.getStatus() == PackageStatusEnum.RECEIVED) {
                    doProcess(pack);
                }
            } catch (SlaveException e) {
                transactionStatus.setRollbackOnly();
                if (SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR == e.getCode()
                    || SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT == e.getCode()
                    || SlaveErrorEnum.SLAVE_LAST_PACKAGE_NOT_FINISH == e.getCode()) {
                    return false;
                }
                log.error("slave exception. ", e);
            } catch (Throwable e) {
                transactionStatus.setRollbackOnly();
                if (e instanceof CannotAcquireLockException) {
                    log.warn("cannot acquire package lock, height={}", height);
                } else {
                    log.error("package process exception. ", e);
                }
                return false;
            }
            return true;
        });
        if (result) {
            /**
             * update processed block height
             */
            updateProcessedHeight(height);
        }
    }

    /**
     * really process
     *
     * @param pack
     */
    private void doProcess(Package pack) {
        // check next package height
        if (!pack.getHeight().equals(blockRepository.getMaxHeight() + 1)) {
            log.warn("package.height: {} is unequal db.height:{}", pack.getHeight(),
                blockRepository.getMaxHeight() + 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT);
        }
        PackContext packContext = packageService.createPackContext(pack);
        // do persist
        packageService.process(packContext, false, false);
    }

    /**
     * get max block height of processed
     *
     * @return
     */
    public Long getMaxHeight() {
        return processedHeight.get();
    }

    /**
     * initializing processed block height
     */
    private void initProcessedHeight() {
        Long currentHeight = blockRepository.getMaxHeight();
        updateProcessedHeight(currentHeight);
    }

    /**
     * update processed block height
     *
     * @param height
     */
    private void updateProcessedHeight(Long height) {
        processedHeight.set(height);
    }
}
