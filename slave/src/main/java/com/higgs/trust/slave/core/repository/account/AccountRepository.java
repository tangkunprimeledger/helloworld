package com.higgs.trust.slave.core.repository.account;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.enums.account.AccountStateEnum;
import com.higgs.trust.slave.api.enums.account.ChangeDirectionEnum;
import com.higgs.trust.slave.api.vo.AccountInfoVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.repository.DataIdentityRepository;
import com.higgs.trust.slave.dao.account.AccountDcRecordDao;
import com.higgs.trust.slave.dao.account.AccountDetailDao;
import com.higgs.trust.slave.dao.account.AccountInfoDao;
import com.higgs.trust.slave.dao.account.AccountJDBCDao;
import com.higgs.trust.slave.dao.po.account.AccountDcRecordPO;
import com.higgs.trust.slave.dao.po.account.AccountDetailPO;
import com.higgs.trust.slave.dao.po.account.AccountInfoPO;
import com.higgs.trust.slave.dao.po.account.AccountInfoWithOwnerPO;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.account.AccountDcRecord;
import com.higgs.trust.slave.model.bo.account.AccountDetail;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import com.higgs.trust.slave.model.convert.DataIdentityConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-10
 */
@Repository @Slf4j public class AccountRepository {
    @Autowired AccountInfoDao accountInfoDao;
    @Autowired AccountDetailDao accountDetailDao;
    @Autowired AccountDcRecordDao accountDcRecordDao;
    @Autowired DataIdentityRepository dataIdentityRepository;
    @Autowired AccountJDBCDao accountJDBCDao;
    /**
     * query account info by account no
     *
     * @param accountNo
     * @param forUpdate
     * @return
     */
    public AccountInfo queryAccountInfo(String accountNo, boolean forUpdate) {
        AccountInfoPO accountInfo = accountInfoDao.queryByAccountNo(accountNo, forUpdate);
        return BeanConvertor.convertBean(accountInfo, AccountInfo.class);
    }

    /**
     * batch query the account info
     *
     * @param accountNos
     * @return
     */
    public List<AccountInfoVO> queryByAccountNos(List<String> accountNos) {
        List<AccountInfoPO> list = accountInfoDao.queryByAccountNos(accountNos);
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
        accountInfo.setCreateTime(new Date());
        return accountInfo;
    }

    /**
     * open an new account
     *
     * @param openAccount
     */
    public AccountInfo openAccount(OpenAccount openAccount) {
        // build and add account info
        AccountInfo accountInfo = buildAccountInfo(openAccount);
        AccountInfoPO accountInfoPO = BeanConvertor.convertBean(accountInfo, AccountInfoPO.class);
        try {
            accountInfoDao.add(accountInfoPO);
        } catch (DuplicateKeyException e) {
            log.error("[openAccount.persist] is idempotent for accountNo:{}", openAccount.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        // add data identity
        DataIdentity dataIdentity = DataIdentityConvert
            .buildDataIdentity(openAccount.getAccountNo(), openAccount.getChainOwner(), openAccount.getDataOwner());
        dataIdentityRepository.save(dataIdentity);
        return accountInfo;
    }

    /**
     * operation account balance,increase or decrease
     *
     * @param accountNo
     * @param changeDirectionEnum
     * @param happenAmount
     */
    public void operationAccountBalance(String accountNo, ChangeDirectionEnum changeDirectionEnum,
        BigDecimal happenAmount) {
        int r = 0;
        //change account balance
        if (StringUtils.equals(changeDirectionEnum.getCode(), ChangeDirectionEnum.INCREASE.getCode())) {
            r = accountInfoDao.increaseBalance(accountNo, happenAmount);
        } else if (StringUtils.equals(changeDirectionEnum.getCode(), ChangeDirectionEnum.DECREASE.getCode())) {
            r = accountInfoDao.decreaseBalance(accountNo, happenAmount);
        }
        if (r == 0) {
            log.error("[operationAccountBalance]change account balance is fail,accountNo:{}", accountNo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_CHANGE_BALANCE_ERROR);
        }
    }

    /**
     * create new account detail
     *
     * @param accountDetail
     */
    public void createAccountDetail(AccountDetail accountDetail) {
        AccountDetailPO detailPO = BeanConvertor.convertBean(accountDetail, AccountDetailPO.class);
        accountDetailDao.add(detailPO);
    }

    /**
     * create new account DC record
     *
     * @param accountDcRecord
     */
    public void createAccountDCRecord(AccountDcRecord accountDcRecord) {
        AccountDcRecordPO accountDcRecordPO = BeanConvertor.convertBean(accountDcRecord, AccountDcRecordPO.class);
        accountDcRecordDao.add(accountDcRecordPO);
    }

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
            log.info("[batchInsert] accountInfos is empty");
            return;
        }
        try {
            Profiler.enter("[batchInsert accountInfo]");
            int r = accountJDBCDao.batchInsertAccount(accountInfos);
            Profiler.release();
            if (r != accountInfos.size()) {
                log.info("[batchInsert]the number of update rows is different from the original number");
                throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchInsert] has idempotent for accountInfos:{}", accountInfos);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        } finally {
            Profiler.release();
            Profiler.logDump();
        }
    }

    /**
     * batch update
     *
     * @param accountInfos
     */
    public void batchUpdate(List<AccountInfo> accountInfos) {
        if (CollectionUtils.isEmpty(accountInfos)) {
            log.info("[batchUpdate] accountInfos is empty");
            return;
        }
        try {
            Profiler.enter("[batchUpdate accountInfo]");
            int r = accountJDBCDao.batchUpdateAccount(accountInfos);
            Profiler.release();
            if (r != accountInfos.size()) {
                log.info("[batchUpdate]the number of update rows is different from the original number");
                throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
            }
        } finally {
            Profiler.release();
            Profiler.logDump();
        }
    }

    /**
     * batch insert account detail
     *
     * @param accountDetails
     */
    public void batchInsertAccountDetail(List<AccountDetail> accountDetails) {
        if (CollectionUtils.isEmpty(accountDetails)) {
            log.info("[batchInsertAccountDetail] accountDetails is empty");
            return;
        }
        try {
            Profiler.enter("[batchInsert accountDetail]");
            int r = accountJDBCDao.batchInsertAccountDetail(accountDetails);
            Profiler.release();
            if (r != accountDetails.size()) {
                log.info("[batchInsertAccountDetail]the number of update rows is different from the original number");
                throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchInsertAccountDetail] has idempotent for accountDetails:{}", accountDetails);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        } finally {
            Profiler.release();
            Profiler.logDump();
        }
    }

    /**
     * batch insert DC record
     *
     * @param dcRecords
     */
    public void batchInsertDcRecords(List<AccountDcRecord> dcRecords) {
        if (CollectionUtils.isEmpty(dcRecords)) {
            log.info("[batchInsertDcRecords] dcRecords is empty");
            return;
        }
        try {
            Profiler.enter("[batchInsert accountDcRecords]");
            int r = accountJDBCDao.batchInsertDcRecords(dcRecords);
            Profiler.release();
            if (r != dcRecords.size()) {
                log.info("[batchInsertDcRecords]the number of update rows is different from the original number");
                throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchInsertDcRecords] has idempotent for dcRecords:{}", dcRecords);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        } finally {
            Profiler.release();
            Profiler.logDump();
        }
    }
}
