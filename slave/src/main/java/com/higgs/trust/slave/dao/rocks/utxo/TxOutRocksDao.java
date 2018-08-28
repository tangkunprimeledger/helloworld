package com.higgs.trust.slave.dao.rocks.utxo;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
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
public class TxOutRocksDao extends RocksBaseDao<String, TxOutPO>{
    @Override protected String getColumnFamilyName() {
        return "txOut";
    }

    public int batchInsert(List<TxOutPO> txOutPOList) {
        if (CollectionUtils.isEmpty(txOutPOList)) {
            return 0;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[TxOutRocksDao.batchInsert] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (TxOutPO po : txOutPOList) {
            String key = po.getTxId() + "_" + po.getIndex() + "_" + po.getActionIndex();
            if (null == po.getCreateTime()) {
                po.setCreateTime(new Date());
            } else {
                po.setUpdateTime(new Date());
            }
            batchPut(batch, key, po);
        }
        return txOutPOList.size();
    }
}
