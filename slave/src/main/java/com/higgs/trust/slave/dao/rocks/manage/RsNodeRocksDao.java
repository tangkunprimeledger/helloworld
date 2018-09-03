package com.higgs.trust.slave.dao.rocks.manage;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.manage.RsNodePO;
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
public class RsNodeRocksDao extends RocksBaseDao<RsNodePO> {
    @Override protected String getColumnFamilyName() {
        return "rsNode";
    }

    public int batchInsert(List<RsNodePO> rsNodePOList) {
        if (CollectionUtils.isEmpty(rsNodePOList)) {
            return 0;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[RsNodeRocksDao.batchInsert] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (RsNodePO po : rsNodePOList) {
            if (null == po.getCreateTime()) {
                po.setCreateTime(new Date());
            } else {
                po.setUpdateTime(new Date());
            }
            batchPut(batch, po.getRsId(), po);
        }

        return rsNodePOList.size();
    }
}
