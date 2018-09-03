package com.higgs.trust.slave.dao.rocks.account;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.account.AccountFreezeRecordPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class AccountFreezeRecordRocksDao extends RocksBaseDao<AccountFreezeRecordPO> {

    @Override protected String getColumnFamilyName() {
        return "accountFreezeRecord";
    }

    public void batchInsert(List<AccountFreezeRecordPO> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return;
        }

        WriteBatch writeBatch = ThreadLocalUtils.getWriteBatch();
        if (null == writeBatch) {
            log.error("[AccountFreezeRecordRocksDao.batchInsert] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (AccountFreezeRecordPO po : pos) {
            String key = po.getBizFlowNo() + Constant.SPLIT_SLASH + po.getAccountNo();
            if (null == po.getCreateTime()) {
                po.setCreateTime(new Date());
            } else {
                po.setUpdateTime(new Date());
            }
            batchPut(writeBatch, key, po);
        }
    }

    public void decreaseAmount(String bizFlowNo, String accountNo, BigDecimal amount) {
        String key = bizFlowNo + Constant.SPLIT_SLASH + accountNo;
        AccountFreezeRecordPO po = get(key);
        if (null == po) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
        }

        BigDecimal newAmount = po.getAmount().subtract(amount);
        if (BigDecimal.ZERO.compareTo(newAmount) > 0) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_UNFREEZE_ERROR);
        }
        po.setAmount(newAmount);
        po.setUpdateTime(new Date());

        put(key, po);
    }
}
