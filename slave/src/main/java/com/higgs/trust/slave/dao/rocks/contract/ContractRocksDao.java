package com.higgs.trust.slave.dao.rocks.contract;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.contract.ContractPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class ContractRocksDao extends RocksBaseDao<ContractPO>{
    @Override protected String getColumnFamilyName() {
        return "contract";
    }

    public int batchInsert(List<ContractPO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[ContractRocksDao.batchInsert] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (ContractPO po : list) {
            po.setCreateTime(new Date());
            batchPut(batch, po.getAddress(), po);
        }

        return list.size();
    }
}
