package com.higgs.trust.slave.dao.rocks.account;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.account.AccountInfoPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tangfashuang
 * @desc key: accountNo, value: AccountInfoPO
 */
@Service
@Slf4j
public class AccountInfoRocksDao extends RocksBaseDao<AccountInfoPO> {

    @Override protected String getColumnFamilyName() {
        return "accountInfo";
    }

    public void freeze(String accountNo, BigDecimal amount) {
        AccountInfoPO po = get(accountNo);
        if (null == po) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
        }

        BigDecimal balance = po.getBalance();
        BigDecimal freezeAmount = po.getFreezeAmount();
        BigDecimal newFreezeAmount = freezeAmount.add(amount);

        if (BigDecimal.ZERO.compareTo(balance.subtract(amount)) > 0) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_BALANCE_IS_NOT_ENOUGH_ERROR);
        }

        if ( BigDecimal.ZERO.compareTo(newFreezeAmount) > 0) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_FREEZE_AMOUNT_ERROR);
        }

        po.setFreezeAmount(newFreezeAmount);
        po.setDetailFreezeNo(po.getDetailNo() + 1);
        po.setUpdateTime(new Date());

        put(accountNo, po);
    }

    public void unFreeze(String accountNo, BigDecimal amount) {
        AccountInfoPO po = get(accountNo);
        if (null == po) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
        }

        BigDecimal freezeAmount = po.getFreezeAmount();
        BigDecimal newFreezeAmount = freezeAmount.subtract(amount);

        if (BigDecimal.ZERO.compareTo(newFreezeAmount) > 0) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_UNFREEZE_AMOUNT_ERROR);
        }

        po.setFreezeAmount(newFreezeAmount);
        po.setDetailFreezeNo(po.getDetailNo() + 1);
        po.setUpdateTime(new Date());

        put(accountNo, po);
    }

    public void batchInsert(List<AccountInfoPO> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[AccountInfoRocksDao.batchInsert] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (AccountInfoPO po : pos) {
            if (null == po.getCreateTime()) {
                po.setCreateTime(new Date());
            } else {
                po.setUpdateTime(new Date());
            }
            txPut(tx, po.getAccountNo(), po);
        }

    }

    public List<AccountInfoPO> queryByAccountNos(List<String> accountNos) {
        if (CollectionUtils.isEmpty(accountNos)) {
            return null;
        }

        Map<String, AccountInfoPO> resultMap = multiGet(accountNos);
        if (MapUtils.isEmpty(resultMap)) {
            return null;
        }

        List<AccountInfoPO> pos = new ArrayList<>(resultMap.size());
        for (String key : resultMap.keySet()) {
            pos.add(resultMap.get(key));
        }

        return pos;
    }
}
