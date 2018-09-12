package com.higgs.trust.slave.dao.rocks.config;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.config.SystemPropertyPO;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class SystemPropertyRocksDao extends RocksBaseDao<SystemPropertyPO> {
    @Override protected String getColumnFamilyName() {
        return "systemProperty";
    }

    public void saveWithTransaction(String key, String value, String desc) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[SystemPropertyRocksDao.saveWithTransaction] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        SystemPropertyPO po = get(key);
        if (null == po) {
            SystemPropertyPO systemPropertyPO = new SystemPropertyPO();
            systemPropertyPO.setKey(key);
            systemPropertyPO.setValue(value);
            systemPropertyPO.setDesc(desc);
            systemPropertyPO.setCreateTime(new Date());
            txPut(tx, key, systemPropertyPO);
        } else {
            po.setValue(value);
            po.setUpdateTime(new Date());
            txPut(tx, key, po);
        }
    }
}