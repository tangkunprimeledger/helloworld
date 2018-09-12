package com.higgs.trust.slave.dao.rocks.dataidentity;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.DataIdentityPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 * @desc key: identity, value: dataIdentityPO
 */
@Service
@Slf4j
public class DataIdentityRocksDao extends RocksBaseDao<DataIdentityPO>{
    @Override protected String getColumnFamilyName() {
        return "dataIdentity";
    }

    public int batchInsert(List<DataIdentityPO> dataIdentityPOList) {
        if (CollectionUtils.isEmpty(dataIdentityPOList)) {
            return 0;
        }
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[DataIdentityRocksDao.batchInsert] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (DataIdentityPO po : dataIdentityPOList) {
            po.setCreateTime(new Date());
            txPut(tx, po.getIdentity(), po);
        }
        return dataIdentityPOList.size();

    }
}
