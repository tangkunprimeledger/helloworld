package com.higgs.trust.slave.core.repository.account;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.api.enums.account.AccountStateEnum;
import com.higgs.trust.slave.api.vo.AccountInfoVO;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.DataIdentityRepository;
import com.higgs.trust.slave.dao.mysql.account.AccountDcRecordDao;
import com.higgs.trust.slave.dao.mysql.account.AccountDetailDao;
import com.higgs.trust.slave.dao.mysql.account.AccountInfoDao;
import com.higgs.trust.slave.dao.mysql.account.AccountJDBCDao;
import com.higgs.trust.slave.dao.po.account.AccountDcRecordPO;
import com.higgs.trust.slave.dao.po.account.AccountDetailPO;
import com.higgs.trust.slave.dao.po.account.AccountInfoPO;
import com.higgs.trust.slave.dao.po.account.AccountInfoWithOwnerPO;
import com.higgs.trust.slave.dao.rocks.account.AccountDcRecordRocksDao;
import com.higgs.trust.slave.dao.rocks.account.AccountDetailRocksDao;
import com.higgs.trust.slave.dao.rocks.account.AccountInfoRocksDao;
import com.higgs.trust.slave.model.bo.account.AccountDcRecord;
import com.higgs.trust.slave.model.bo.account.AccountDetail;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-10
 */
@Repository @Slf4j public class AccountRepository {
    @Autowired AccountInfoDao accountInfoDao;
    @Autowired AccountInfoRocksDao accountInfoRocksDao;
    @Autowired AccountDetailDao accountDetailDao;
    @Autowired AccountDetailRocksDao accountDetailRocksDao;
    @Autowired AccountDcRecordDao accountDcRecordDao;
    @Autowired AccountDcRecordRocksDao accountDcRecordRocksDao;
    @Autowired DataIdentityRepository dataIdentityRepository;
    @Autowired AccountJDBCDao accountJDBCDao;
    @Autowired InitConfig initConfig;
    /**
     * query account info by account no
     *
     * @param accountNo
     * @param forUpdate
     * @return
     */
    public AccountInfo queryAccountInfo(String accountNo, boolean forUpdate) {
        AccountInfoPO accountInfo;
        if (initConfig.isUseMySQL()) {
            accountInfo = accountInfoDao.queryByAccountNo(accountNo, forUpdate);
        } else {
            //for update 参数未使用
            accountInfo = accountInfoRocksDao.get(accountNo);
        }
        return BeanConvertor.convertBean(accountInfo, AccountInfo.class);
    }

    /**
     * batch query the account info
     *
     * @param accountNos
     * @return
     */
    public List<AccountInfoVO> queryByAccountNos(List<String> accountNos) {
        List<AccountInfoPO> list;
        if (initConfig.isUseMySQL()) {
            list = accountInfoDao.queryByAccountNos(accountNos);
        } else {
            list = accountInfoRocksDao.queryByAccountNos(accountNos);
        }
        return BeanConvertor.convertList(list, AccountInfoVO.class);
    }

    /**
     * build an new account info
     *
     * @param openAccount
     * @return
     */
    public AccountInfo buildAccountInfo(OpenAccount openAccount) {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setAccountNo(openAccount.getAccountNo());
        accountInfo.setCurrency(openAccount.getCurrency());
        accountInfo.setBalance(BigDecimal.ZERO);
        accountInfo.setFreezeAmount(BigDecimal.ZERO);
        accountInfo.setFundDirection(openAccount.getFundDirection().getCode());
        accountInfo.setDetailNo(0L);
        accountInfo.setDetailFreezeNo(0L);
        accountInfo.setStatus(AccountStateEnum.NORMAL.getCode());
        return accountInfo;
    }

    /**
     * for explorer
     * @param accountNo
     * @param dataOwner
     * @param pageNo
     * @param pageSize
     * @return
     */
    public List<AccountInfoVO> queryAccountInfoWithOwner(String accountNo, String dataOwner, Integer pageNo,
        Integer pageSize) {
        if (null != accountNo) {
            accountNo = accountNo.trim();
        }

        if (null != dataOwner) {
            dataOwner = dataOwner.trim();
        }

        List<AccountInfoWithOwnerPO> list =
            accountInfoDao.queryAccountInfoWithOwner(accountNo, dataOwner, (pageNo - 1) * pageSize, pageSize);
        return BeanConvertor.convertList(list, AccountInfoVO.class);
    }

    /**
     * for explorer
     * @param accountNo
     * @param dataOwner
     * @return
     */
    public long countAccountInfoWithOwner(String accountNo, String dataOwner) {
        if (null != accountNo) {
            accountNo = accountNo.trim();
        }

        if (null != dataOwner) {
            dataOwner = dataOwner.trim();
        }

        return accountInfoDao.countAccountInfoWithOwner(accountNo, dataOwner);
    }

    /**
     * batch insert
     *
     * @param accountInfos
     */
    public void batchInsert(List<AccountInfo> accountInfos) {
        if (CollectionUtils.isEmpty(accountInfos)) {
            return;
        }
        try {
            Profiler.enter("[batchInsert accountInfo]");
            if (initConfig.isUseMySQL()) {
                int r = accountJDBCDao.batchInsertAccount(accountInfos);
                if (r != accountInfos.size()) {
                    log.info("[batchInsert]the number of update rows is different from the original number");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
                }
            } else {
                accountInfoRocksDao.batchInsert(
                    BeanConvertor.convertList(accountInfos, AccountInfoPO.class));
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchInsert] has idempotent for accountInfos:{}", accountInfos);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        } finally {
            Profiler.release();
        }
    }

    /**
     * batch update
     *
     * @param accountInfos
     */
    public void batchUpdate(List<AccountInfo> accountInfos) {
        if (CollectionUtils.isEmpty(accountInfos)) {
            return;
        }
        try {
            Profiler.enter("[batchUpdate accountInfo]");
            if (initConfig.isUseMySQL()) {
                int r = accountJDBCDao.batchUpdateAccount(accountInfos);
                if (r != accountInfos.size()) {
                    log.info("[batchUpdate]the number of update rows is different from the original number");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
                }
            } else {
                accountInfoRocksDao.batchInsert(
                    BeanConvertor.convertList(accountInfos, AccountInfoPO.class));
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * batch insert account detail
     *
     * @param accountDetails
     */
    public void batchInsertAccountDetail(List<AccountDetail> accountDetails) {
        if (CollectionUtils.isEmpty(accountDetails)) {
            return;
        }
        try {
            Profiler.enter("[batchInsert accountDetail]");
            if (initConfig.isUseMySQL()) {
                int r = accountJDBCDao.batchInsertAccountDetail(accountDetails);
                if (r != accountDetails.size()) {
                    log.info("[batchInsertAccountDetail]the number of update rows is different from the original number");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
                }
            } else {
                accountDetailRocksDao.batchInsert(
                    BeanConvertor.convertList(accountDetails, AccountDetailPO.class));
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchInsertAccountDetail] has idempotent for accountDetails:{}", accountDetails);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        } finally {
            Profiler.release();
        }
    }

    /**
     * batch insert DC record
     *
     * @param dcRecords
     */
    public void batchInsertDcRecords(List<AccountDcRecord> dcRecords) {
        if (CollectionUtils.isEmpty(dcRecords)) {
            return;
        }
        try {
            Profiler.enter("[batchInsert accountDcRecords]");
            if (initConfig.isUseMySQL()) {
                int r = accountJDBCDao.batchInsertDcRecords(dcRecords);
                if (r != dcRecords.size()) {
                    log.info("[batchInsertDcRecords]the number of update rows is different from the original number");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
                }
            } else {
                accountDcRecordRocksDao.batchInsert(
                    BeanConvertor.convertList(dcRecords, AccountDcRecordPO.class));
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchInsertDcRecords] has idempotent for dcRecords:{}", dcRecords);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        } finally {
            Profiler.release();
        }
    }
}
