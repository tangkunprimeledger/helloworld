package com.higgs.trust.slave.core.repository.account;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.dao.account.AccountDetailFreezeDao;
import com.higgs.trust.slave.dao.account.AccountFreezeRecordDao;
import com.higgs.trust.slave.dao.account.AccountInfoDao;
import com.higgs.trust.slave.dao.account.AccountJDBCDao;
import com.higgs.trust.slave.dao.po.account.AccountDetailFreezePO;
import com.higgs.trust.slave.dao.po.account.AccountFreezeRecordPO;
import com.higgs.trust.slave.model.bo.account.AccountDetailFreeze;
import com.higgs.trust.slave.model.bo.account.AccountFreeze;
import com.higgs.trust.slave.model.bo.account.AccountFreezeRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
@Repository @Slf4j public class FreezeRepository {
    @Autowired AccountInfoDao accountInfoDao;
    @Autowired AccountFreezeRecordDao accountFreezeRecordDao;
    @Autowired AccountDetailFreezeDao accountDetailFreezeDao;
    @Autowired AccountJDBCDao accountJDBCDao;
    /**
     * query account freeze record
     *
     * @param bizFlowNo
     * @param accountNo
     * @return
     */
    public AccountFreezeRecord queryByFlowNoAndAccountNo(String bizFlowNo, String accountNo) {
        AccountFreezeRecordPO accountFreezeRecordPO =
            accountFreezeRecordDao.queryByFlowNoAndAccountNo(bizFlowNo, accountNo);
        return BeanConvertor.convertBean(accountFreezeRecordPO, AccountFreezeRecord.class);
    }

    /**
     * build a new record
     *
     * @param accountFreeze
     * @param blockHeight
     * @return
     */
    public AccountFreezeRecord build(AccountFreeze accountFreeze, Long blockHeight) {
        AccountFreezeRecord accountFreezeRecord = new AccountFreezeRecord();
        accountFreezeRecord.setBizFlowNo(accountFreeze.getBizFlowNo());
        accountFreezeRecord.setAccountNo(accountFreeze.getAccountNo());
        accountFreezeRecord.setAmount(accountFreeze.getAmount());
        accountFreezeRecord.setBlockHeight(blockHeight);
        accountFreezeRecord.setContractAddr(accountFreeze.getContractAddr());
        accountFreezeRecord.setCreateTime(new Date());
        return accountFreezeRecord;
    }

    /**
     * create new account freeze record
     *
     * @param accountFreezeRecord
     */
    public void createFreezeRecord(AccountFreezeRecord accountFreezeRecord) {
        AccountFreezeRecordPO accountFreezeRecordPO =
            BeanConvertor.convertBean(accountFreezeRecord, AccountFreezeRecordPO.class);
        try {
            accountFreezeRecordDao.add(accountFreezeRecordPO);
        } catch (DuplicateKeyException e) {
            log.error(
                "[createFreezeRecord] freeze is fail accountNo and bizFlowNo is already exists, bizFlowNo:{},accountNo:{}",
                accountFreezeRecord.getBizFlowNo(), accountFreezeRecord.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_FREEZE_ERROR);
        }
    }

    /**
     * freeze balance for account
     *
     * @param accountNo
     * @param amount
     */
    public void freeze(String accountNo, BigDecimal amount) {
        int r = accountInfoDao.freeze(accountNo, amount);
        if (r != 1) {
            log.error("[freeze] freeze is fail accountNo:{}", accountNo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_FREEZE_ERROR);
        }
    }

    /**
     * unfreeze balance for account
     *
     * @param accountNo
     * @param amount
     */
    public void unfreeze(String accountNo, BigDecimal amount) {
        int r = accountInfoDao.unfreeze(accountNo, amount);
        if (r != 1) {
            log.error("[unfreeze] unfreeze is fail accountNo:{}", accountNo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_FREEZE_ERROR);
        }
    }

    /**
     * create freeze detail
     *
     * @param accountDetailFreeze
     */
    public void createFreezeDetail(AccountDetailFreeze accountDetailFreeze) {
        AccountDetailFreezePO detailFreeze =
            BeanConvertor.convertBean(accountDetailFreeze, AccountDetailFreezePO.class);
        accountDetailFreezeDao.add(detailFreeze);
    }

    /**
     * decrease amount for freeze
     *
     * @param id
     * @param amount
     */
    public void decreaseAmount(Long id, BigDecimal amount) {
        int r = accountFreezeRecordDao.decreaseAmount(id, amount);
        if (r != 1) {
            log.error("[decreaseAmount] unfreeze is fail recordId:{}", id);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_UNFREEZE_ERROR);
        }
    }

    /**
     * batch insert
     *
     * @param accountFreezeRecords
     */
    public void batchInsert(List<AccountFreezeRecord> accountFreezeRecords) {
        if (CollectionUtils.isEmpty(accountFreezeRecords)) {
            return;
        }
        try {
            Profiler.enter("[batchInsert accountFreezeRecords]");
            int r = accountJDBCDao.batchInsertFreezeRecord(accountFreezeRecords);
            if (r != accountFreezeRecords.size()) {
                log.info("[batchInsert]the number of update rows is different from the original number");
                throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchInsert] has idempotent for accountFreezeRecords:{}", accountFreezeRecords);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        } finally {
            Profiler.release();
        }
    }

    /**
     * batch update
     *
     * @param accountFreezeRecords
     */
    public void batchUpdate(List<AccountFreezeRecord> accountFreezeRecords) {
        if (CollectionUtils.isEmpty(accountFreezeRecords)) {
            return;
        }
        try {
            Profiler.enter("[batchUpdate accountFreezeRecords]");
            int r = accountJDBCDao.batchUpdateFreezeRecord(accountFreezeRecords);
            if (r != accountFreezeRecords.size()) {
                log.info("[batchUpdate]the number of update rows is different from the original number");
                throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * batch insert detail freeze
     *
     * @param detailFreezes
     */
    public void batchInsertDetailFreezes(List<AccountDetailFreeze> detailFreezes) {
        if (CollectionUtils.isEmpty(detailFreezes)) {
            return;
        }
        try {
            Profiler.enter("[batchInsert detailFreeze]");
            int r = accountJDBCDao.batchInsertDetailFreezes(detailFreezes);
            if (r != detailFreezes.size()) {
                log.info("[batchInsertDetailFreezes]the number of update rows is different from the original number");
                throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchInsertDetailFreezes] has idempotent for detailFreezes:{}", detailFreezes);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        } finally {
            Profiler.release();
        }
    }
}
