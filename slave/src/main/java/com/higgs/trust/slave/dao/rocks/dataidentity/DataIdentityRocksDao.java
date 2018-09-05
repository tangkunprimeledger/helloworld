package com.higgs.trust.slave.dao.rocks.dataidentity;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.DataIdentityPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.WriteBatch;
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
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[DataIdentityRocksDao.batchInsert] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (DataIdentityPO po : dataIdentityPOList) {
            po.setCreateTime(new Date());
            batchPut(batch, po.getIdentity(), po);
        }
        return dataIdentityPOList.size();

    }
}
