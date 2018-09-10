package com.higgs.trust.slave.dao.rocks.block;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.block.BlockPO;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class BlockTestRocksDao extends RocksBaseDao<BlockPO> {
    @Override protected String getColumnFamilyName() {
        return "blockTest";
    }

    public void save(BlockPO po) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[BlockTestRocksDao.save] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        String height = String.valueOf(po.getHeight());
        if (keyMayExist(height) && null != get(height)) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }
        po.setSignedTxs(null);

        batchPut(batch, height, po);
    }
}
