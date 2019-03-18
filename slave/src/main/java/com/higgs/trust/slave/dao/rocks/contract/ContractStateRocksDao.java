package com.higgs.trust.slave.dao.rocks.contract;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.contract.ContractStatePO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class ContractStateRocksDao extends RocksBaseDao<ContractStatePO> {
    @Override protected String getColumnFamilyName() {
        return "contractState";
    }

    public int batchInsert(Collection<ContractStatePO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[ContractStateRocksDao.batchInsert] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (ContractStatePO po : list) {
            po.setUpdateTime(new Date());
            txPut(tx, po.getAddress(), po);
        }
        return list.size();
    }

    public void save(ContractStatePO po) {
        String address = po.getAddress();
        if (keyMayExist(address) && null != get(address)) {
            log.error("[ContractStateRocksDao.save] contract state is already exist, address={}", address);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }
        put(po.getAddress(), po);
    }
}
