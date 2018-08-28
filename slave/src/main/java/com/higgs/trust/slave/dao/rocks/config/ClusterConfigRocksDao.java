package com.higgs.trust.slave.dao.rocks.config;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
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
public class ClusterConfigRocksDao extends RocksBaseDao<String, ClusterConfigPO>{
    @Override protected String getColumnFamilyName() {
        return "clusterConfig";
    }

    public void save(ClusterConfigPO clusterConfigPO) {
        String key = clusterConfigPO.getClusterName();
        if (null != get(key)) {
            log.error("[ClusterConfigRocksDao.save] cluster config is exist, clusterName={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }

        clusterConfigPO.setCreateTime(new Date());
        put(key, clusterConfigPO);
    }

    public int batchInsert(List<ClusterConfigPO> clusterConfigPOList) {
        if (CollectionUtils.isEmpty(clusterConfigPOList)) {
            return 0;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[ClusterConfigRocksDao.batchInsert] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (ClusterConfigPO po : clusterConfigPOList) {
            if (null == po.getCreateTime()) {
                po.setCreateTime(new Date());
            } else {
                po.setUpdateTime(new Date());
            }
            batchPut(batch, po.getClusterName(), po);
        }
        return clusterConfigPOList.size();
    }
}
