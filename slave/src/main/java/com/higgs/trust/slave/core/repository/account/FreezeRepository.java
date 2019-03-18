package com.higgs.trust.slave.core.repository.account;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.mysql.account.AccountDetailFreezeDao;
import com.higgs.trust.slave.dao.mysql.account.AccountFreezeRecordDao;
import com.higgs.trust.slave.dao.mysql.account.AccountInfoDao;
import com.higgs.trust.slave.dao.mysql.account.AccountJDBCDao;
import com.higgs.trust.slave.dao.po.account.AccountDetailFreezePO;
import com.higgs.trust.slave.dao.po.account.AccountFreezeRecordPO;
import com.higgs.trust.slave.dao.rocks.account.AccountDetailFreezeRocksDao;
import com.higgs.trust.slave.dao.rocks.account.AccountFreezeRecordRocksDao;
import com.higgs.trust.slave.dao.rocks.account.AccountInfoRocksDao;
import com.higgs.trust.slave.model.bo.account.AccountDetailFreeze;
import com.higgs.trust.slave.model.bo.account.AccountFreeze;
import com.higgs.trust.slave.model.bo.account.AccountFreezeRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-10
 */
@Repository @Slf4j public class FreezeRepository {
    @Autowired AccountInfoDao accountInfoDao;
    @Autowired AccountInfoRocksDao accountInfoRocksDao;
    @Autowired AccountFreezeRecordRocksDao accountFreezeRecordRocksDao;
    @Autowired AccountFreezeRecordDao accountFreezeRecordDao;
    @Autowired AccountDetailFreezeRocksDao accountDetailFreezeRocksDao;
    @Autowired AccountDetailFreezeDao accountDetailFreezeDao;
    @Autowired AccountJDBCDao accountJDBCDao;
    @Autowired InitConfig initConfig;
    /**
     * query account freeze record
     *
     * @param bizFlowNo
     * @param accountNo
     * @return
     */
    public AccountFreezeRecord queryByFlowNoAndAccountNo(String bizFlowNo, String accountNo) {
        AccountFreezeRecordPO accountFreezeRecordPO;
        if (initConfig.isUseMySQL()) {
            accountFreezeRecordPO = accountFreezeRecordDao.queryByFlowNoAndAccountNo(bizFlowNo, accountNo);
        } else {
            accountFreezeRecordPO = accountFreezeRecordRocksDao.get(bizFlowNo + "_"+ accountNo);
        }
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
            if (initConfig.isUseMySQL()) {
                int r = accountJDBCDao.batchInsertFreezeRecord(accountFreezeRecords);
                if (r != accountFreezeRecords.size()) {
                    log.info("[batchInsert]the number of update rows is different from the original number");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
                }
            } else {
                accountFreezeRecordRocksDao.batchInsert(
                    BeanConvertor.convertList(accountFreezeRecords, AccountFreezeRecordPO.class));
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
            if (initConfig.isUseMySQL()) {
                int r = accountJDBCDao.batchUpdateFreezeRecord(accountFreezeRecords);
                if (r != accountFreezeRecords.size()) {
                    log.info("[batchUpdate]the number of update rows is different from the original number");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
                }
            } else {
                accountFreezeRecordRocksDao.batchInsert(
                    BeanConvertor.convertList(accountFreezeRecords, AccountFreezeRecordPO.class));
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
            if (initConfig.isUseMySQL()) {
                int r = accountJDBCDao.batchInsertDetailFreezes(detailFreezes);
                if (r != detailFreezes.size()) {
                    log.info("[batchInsertDetailFreezes]the number of update rows is different from the original number");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
                }
            } else {
                accountDetailFreezeRocksDao.batchInsert(
                    BeanConvertor.convertList(detailFreezes, AccountDetailFreezePO.class));
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchInsertDetailFreezes] has idempotent for detailFreezes:{}", detailFreezes);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        } finally {
            Profiler.release();
        }
    }
}
