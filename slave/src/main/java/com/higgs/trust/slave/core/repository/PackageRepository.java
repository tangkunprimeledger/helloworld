package com.higgs.trust.slave.core.repository;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.config.SystemPropertyRepository;
import com.higgs.trust.slave.dao.mysql.pack.PackageDao;
import com.higgs.trust.slave.dao.po.pack.PackagePO;
import com.higgs.trust.slave.dao.rocks.pack.PackRocksDao;
import com.higgs.trust.slave.dao.rocks.pack.PackStatusRocksDao;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.config.SystemProperty;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.ReadOptions;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @Description:
 * @author: pengdi
 **/
@Repository @Slf4j public class PackageRepository {

    @Autowired PackageDao packageDao;

    @Autowired PackRocksDao packRocksDao;

    @Autowired PackStatusRocksDao packStatusRocksDao;

    @Autowired SystemPropertyRepository systemPropertyRepository;

    @Autowired PendingTxRepository pendingTxRepository;

    @Autowired InitConfig initConfig;

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

        PackagePO packagePO = convertPackToPackagePO(pack);
        if (initConfig.isUseMySQL()) {
            packageDao.add(packagePO);
        } else {
            packRocksDao.save(packagePO);
            //to store package status
            packStatusRocksDao.save(packagePO.getHeight(), pack.getStatus().getIndex());
        }
    }

    /**
     * update status from repository
     *
     * @param height
     * @param from
     * @param to
     */
    public void updateStatus(Long height, PackageStatusEnum from, PackageStatusEnum to) {
        if (initConfig.isUseMySQL()) {
            int r = packageDao.updateStatus(height, from.getCode(), to.getCode());
            if (r != 1) {
                log.error("[package.updateStatus] has error");
                throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_UPDATE_STATUS_ERROR);
            }
        } else {
            packStatusRocksDao.update(height, from.getIndex(), to.getIndex());
        }
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
        PackagePO packagePO;

        if (initConfig.isUseMySQL()) {
            packagePO = packageDao.queryByHeight(height);
            if (null != packagePO) {
                packagePO.setSignedTxList(pendingTxRepository.getTransactionsByHeight(packagePO.getHeight()));
            }
        } else {
            packagePO = packRocksDao.get(String.valueOf(height));
            if (null != packagePO) {
                packagePO.setStatus(packStatusRocksDao.getStatusByHeight(height));
            }
        }
        return convertPackagePOToPackage(packagePO);
    }

    /**
     * load package from repository
     *
     * @param height
     * @return
     */
    public List<Long> loadHeightList(Long height) {
        List<Long> heights;
        if (initConfig.isUseMySQL()) {
            heights = packageDao.queryHeightListByHeight(height);
        } else {
            heights = packStatusRocksDao.queryHeightListByHeight(height + 1);
        }
        return heights;
    }

    /**
     * load package and lock from repository
     *
     * @param height
     * @return
     */
    public Package loadAndLock(Long height) {
        PackagePO packagePO;

        if (initConfig.isUseMySQL()) {
            packagePO = packageDao.queryByHeightForUpdate(height);
            if (null != packagePO) {
                packagePO.setSignedTxList(pendingTxRepository.getTransactionsByHeight(packagePO.getHeight()));
            }
        } else {
            packagePO = packRocksDao.get(String.valueOf(height));
        }
        return convertPackagePOToPackage(packagePO);
    }

    /**
     * get max height from package
     *
     * @return
     */
    public Long getMaxHeight() {
        if (initConfig.isUseMySQL()) {
            return packageDao.getMaxHeight();
        } else {
            SystemProperty bo = systemPropertyRepository.queryByKey(Constant.MAX_PACK_HEIGHT);
            return bo != null && !StringUtils.isEmpty(bo.getValue()) ? Long.parseLong(bo.getValue()) : null;
        }
    }

     /**
     * get count with package status
     *
     * @param statusSet
     * @return
     */
    public long count(Set<String> statusSet, Long maxBlockHeight) {
        if (initConfig.isUseMySQL()) {
            return packageDao.countWithStatus(statusSet, maxBlockHeight);
        } else {
            SystemProperty bo = systemPropertyRepository.queryByKey(Constant.MAX_PACK_HEIGHT);
            return bo != null && !StringUtils.isEmpty(bo.getValue()) ? Long.parseLong(bo.getValue()) - maxBlockHeight : 0;
        }
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
        packagePO.setSignedTxList(pack.getSignedTxList());
        return packagePO;
    }

    /**
     * convert packagePO to package
     *
     * @param packagePO
     * @return
     */
    private Package convertPackagePOToPackage(PackagePO packagePO) {
        if (null == packagePO) {
            return null;
        }
        Package packageBO = new Package();
        packageBO.setHeight(packagePO.getHeight());
        packageBO.setPackageTime(packagePO.getPackageTime());
        packageBO.setStatus(PackageStatusEnum.getByCode(packagePO.getStatus()));

        packageBO.setSignedTxList(packagePO.getSignedTxList());
        return packageBO;
    }

    /**
     * check package status
     *
     * @param height
     * @param packageStatusEnum
     * @return
     */
    public boolean isPackageStatus(Long height, PackageStatusEnum packageStatusEnum) {
        PackagePO packagePO = null;
        if (initConfig.isUseMySQL()) {
            packagePO = packageDao.queryByHeight(height);
        } else {
            packagePO = packRocksDao.get(String.valueOf(height));
            if (null != packagePO) {
                packagePO.setStatus(packStatusRocksDao.getStatusByHeight(height));
            }
        }
        if (packagePO == null) {
            log.warn("[isPackageStatus] package is null height:{}", height);
            return false;
        }
        PackageStatusEnum status = PackageStatusEnum.getByCode(packagePO.getStatus());
        log.debug("package of DB status:{}, blockHeight:{}", status, height);
        return status == packageStatusEnum;
    }

    /**
     * get max height by status
     *
     * @param status
     * @return
     */
    public Long getMaxHeightByStatus(PackageStatusEnum status) {
        if (initConfig.isUseMySQL()) {
            return packageDao.getMaxHeightByStatus(status.getCode());
        }
        return packStatusRocksDao.getMaxHeightByStatus(status.getIndex());
    }

    /**
     * get min height by status
     *
     * @param status
     * @return
     */
    public Long getMinHeightByStatus(PackageStatusEnum status) {
        if (initConfig.isUseMySQL()) {
            return packageDao.getMinHeightByStatus(status.getCode());
        }
        return packStatusRocksDao.getMinHeightByStatus(status.getIndex());
    }

    /**
     * delete by less than height
     *
     * @param height
     * @return
     */
    public int deleteLessThanHeightAndStatus(Long height,PackageStatusEnum status) {
        if (initConfig.isUseMySQL()) {
            return packageDao.deleteLessThanHeightAndStatus(height, status.getCode());
        }

        List<Long> heights = packStatusRocksDao.queryByPrefix(status.getIndex());
        int count = 0;
        Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
        ThreadLocalUtils.putRocksTx(tx);
        try {
            for (Long h : heights) {
                if (height.compareTo(h) >= 0) {
                    deletePendingTx(height);
                    packRocksDao.batchDelete(h);
                    packStatusRocksDao.batchDelete(h, status.getIndex());
                    count++;
                }
            }
            RocksUtils.txCommit(tx);
        } finally {
            ThreadLocalUtils.clearRocksTx();
        }
        return count;
    }

    public Long getForUpdate(Transaction tx, ReadOptions readOptions, String key, boolean exclusive) {
        return packStatusRocksDao.getForUpdate(tx, readOptions, key, exclusive);
    }

    private void deletePendingTx(Long height) {
        PackagePO po = packRocksDao.get(String.valueOf(height));
        if (po == null || CollectionUtils.isEmpty(po.getSignedTxList())) {
            return;
        }

        pendingTxRepository.batchDelete(po.getSignedTxList());
    }

    /**
     * get height list by status
     * @param status
     * @return
     */
    public List<Long> getBlockHeightsByStatus(PackageStatusEnum status) {
        if (initConfig.isUseMySQL()) {
            return packageDao.getBlockHeightsByStatus(status.getCode());
        }
        return packStatusRocksDao.getBlockHeightsByStatus(status.getIndex());
    }
}
