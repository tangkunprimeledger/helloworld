package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.pack.PackageDao;
import com.higgs.trust.slave.dao.po.pack.PackagePO;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;

/**
 * @Description:
 * @author: pengdi
 **/
@Repository @Slf4j public class PackageRepository {
    @Autowired private TransactionTemplate txRequired;

    @Autowired PackageDao packageDao;

    @Autowired PendingTxRepository pendingTxRepository;

    /**
     * new package from repository
     *
     * @param pack
     */
    public void save(Package pack) {
        if (null == pack) {
            log.error("package is null");
            return;
        }

        //TODO rocks db
        PackagePO packagePO = convertPackToPackagePO(pack);
        packageDao.add(packagePO);
    }

    public PackagePO queryByHeight(Long height) {
        return packageDao.queryByHeight(height);
    }

    /**
     * update status from repository
     *
     * @param height
     * @param from
     * @param to
     */
    public void updateStatus(Long height, PackageStatusEnum from, PackageStatusEnum to) {
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                int r = packageDao.updateStatus(height, from.getCode(), to.getCode());
                if (r != 1) {
                    //check package status
                    PackagePO po = packageDao.queryByHeight(height);
                    if (null != po
                        && StringUtils.equals(to.getCode(), po.getStatus())) {
                        return;
                    }
                    log.error("[package.updateStatus] has error");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_UPDATE_STATUS_ERROR);
                }
            }
        });
    }

    /**
     * load package from repository
     *
     * @param height
     * @return
     */
    public Package load(String height) {
        if (null == height) {
            log.error("load package with null height!");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        Long heightStr = Long.valueOf(height);
        return load(heightStr);
    }

    /**
     * load package from repository
     *
     * @param height
     * @return
     */
    public Package load(Long height) {
        PackagePO packagePO = packageDao.queryByHeight(height);

        if (null == packagePO) {
            return null;
        }

        return convertPackagePOToPackage(packagePO);
    }

    /**
     * load package and lock from repository
     *
     * @param height
     * @return
     */
    public Package loadAndLock(Long height) {

        PackagePO packagePO = packageDao.queryByHeightForUpdate(height);

        if (null == packagePO) {
            return null;
        }

        return convertPackagePOToPackage(packagePO);
    }

    /**
     * get max height from package
     *
     * @return
     */
    public Long getMaxHeight() {
        return packageDao.getMaxHeight();
    }

    /**
     * get package list
     *
     * @return
     */
    public List<Long> getHeightListByStatus(String status) {
        List<Long> heightList = packageDao.queryHeightListByStatus(status);

        if (CollectionUtils.isEmpty(heightList)) {
            return null;
        }

        return heightList;
    }

    /**
     * get package list
     *
     * @return
     */
    public List<Long> getHeightsByStatusAndLimit(String status, int limit) {
        List<Long> heightList = packageDao.queryHeightsByStatusAndLimit(status, limit);

        if (CollectionUtils.isEmpty(heightList)) {
            return null;
        }

        return heightList;
    }

    /**
     * get min height with package status
     *
     * @param statusSet
     * @return
     */
    public Long getMinHeight(Set<String> statusSet, Long maxBlockHeight) {
        return packageDao.getMinHeightWithStatus(statusSet, maxBlockHeight);
    }

    /**
     * get min height with package status and start height
     *
     * @param startHeight
     * @param statusSet
     * @return
     */
    public Long getMinHeight(long startHeight, Set<String> statusSet) {
        return packageDao.getMinHeightWithHeightAndStatus(startHeight, statusSet);
    }

    /**
     * get count with package status
     *
     * @param statusSet
     * @return
     */
    public long count(Set<String> statusSet, Long maxBlockHeight) {
        return packageDao.countWithStatus(statusSet, maxBlockHeight);
    }

    /**
     * convert package to packagePO
     *
     * @param pack
     * @return
     */
    private PackagePO convertPackToPackagePO(Package pack) {
        PackagePO packagePO = new PackagePO();
        packagePO.setHeight(pack.getHeight());
        packagePO.setPackageTime(pack.getPackageTime());
        packagePO.setStatus(pack.getStatus().getCode());
        return packagePO;
    }

    /**
     * convert packagePO to package
     *
     * @param packagePO
     * @return
     */
    private Package convertPackagePOToPackage(PackagePO packagePO) {
        Package packageBO = new Package();
        packageBO.setHeight(packagePO.getHeight());
        packageBO.setPackageTime(packagePO.getPackageTime());
        packageBO.setStatus(PackageStatusEnum.getByCode(packagePO.getStatus()));

        packageBO.setSignedTxList(pendingTxRepository.getTransactionsByHeight(packagePO.getHeight()));
        return packageBO;
    }

}
