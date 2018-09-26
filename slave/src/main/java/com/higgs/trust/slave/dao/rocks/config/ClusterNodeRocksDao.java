package com.higgs.trust.slave.dao.rocks.config;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.config.ClusterNodePO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class ClusterNodeRocksDao extends RocksBaseDao<ClusterNodePO>{
    @Override protected String getColumnFamilyName() {
        return "clusterNode";
    }

    public void saveWithTransaction(ClusterNodePO clusterNodePO) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[ClusterNodeRocksDao.saveWithTransaction] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        String key = clusterNodePO.getNodeName();
        if (keyMayExist(key) && null != get(key)) {
            log.error("[ClusterNodeRocksDao.save] cluster node is exist, nodeName={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }

        clusterNodePO.setCreateTime(new Date());
        txPut(tx, key, clusterNodePO);
    }

    public int batchInsert(List<ClusterNodePO> clusterNodePOList) {
        if (CollectionUtils.isEmpty(clusterNodePOList)) {
            return 0;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[ClusterNodeRocksDao.batchInsert] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (ClusterNodePO po : clusterNodePOList) {
            if (null == po.getCreateTime()) {
                po.setCreateTime(new Date());
            } else {
                po.setUpdateTime(new Date());
            }
            txPut(tx, po.getNodeName(), po);
        }
        return clusterNodePOList.size();
    }
}
