package com.higgs.trust.slave.dao.rocks.manage;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.manage.RsNodePO;
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
public class RsNodeRocksDao extends RocksBaseDao<RsNodePO> {
    @Override protected String getColumnFamilyName() {
        return "rsNode";
    }

    public int batchInsert(List<RsNodePO> rsNodePOList) {
        if (CollectionUtils.isEmpty(rsNodePOList)) {
            return 0;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[RsNodeRocksDao.batchInsert] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (RsNodePO po : rsNodePOList) {
            po.setCreateTime(new Date());
            txPut(tx, po.getRsId(), po);
        }

        return rsNodePOList.size();
    }

    public int batchUpdate(List<RsNodePO> rsNodePOList) {
        if (CollectionUtils.isEmpty(rsNodePOList)) {
            return 0;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[RsNodeRocksDao.batchUpdate] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (RsNodePO po : rsNodePOList) {
            String key = po.getRsId();
            RsNodePO oldPO = get(key);
            if (null == oldPO) {
                log.error("[RsNodeRocksDao.batchUpdate] rs node is not exist. rsId={}", key);
                throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
            }
            oldPO.setStatus(po.getStatus());
            oldPO.setUpdateTime(new Date());
            txPut(tx, key, oldPO);
        }

        return rsNodePOList.size();
    }
}
