package com.higgs.trust.slave.dao.rocks.contract;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.contract.ContractStatePO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.WriteBatch;
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

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[ContractStateRocksDao.batchInsert] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (ContractStatePO po : list) {
            po.setUpdateTime(new Date());
            batchPut(batch, po.getAddress(), po);
        }
        return list.size();
    }

    public void save(ContractStatePO po) {
        String address = po.getAddress();
        if (null != get(address)) {
            log.error("[ContractStateRocksDao.save] contract state is already exist, address={}", address);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }
        put(po.getAddress(), po);
    }
}
