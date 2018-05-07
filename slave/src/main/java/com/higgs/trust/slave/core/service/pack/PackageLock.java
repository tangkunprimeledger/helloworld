package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.api.vo.PackageVO;
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

        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
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
}
