package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class CoreTxProcessRocksDao extends RocksBaseDao<String, CoreTransactionProcessPO>{
    @Override protected String getColumnFamilyName() {
        return "coreTransactionProcess";
    }

    public void save(CoreTransactionProcessPO po) {
        String key = po.getStatus() + Constant.SPLIT_SLASH + po.getTxId();
        if (null != get(key)) {
            log.error("[CoreTxProcessRocksDao.save] core transaction process is exist, key={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }

        po.setCreateTime(new Date());
        put(key, po);
    }

    public void updateStatus(String txId, String from, String to) {
        String key = from + Constant.SPLIT_SLASH + txId;
        //first query
        CoreTransactionProcessPO po = get(key);
        if (null == po) {
            log.error("[CoreTxProcessRocksDao.updateStatus] core transaction process is not exist, key={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }

        po.setUpdateTime(new Date());
        po.setStatus(to);
        String newKey = to + Constant.SPLIT_SLASH + txId;
        //second delete
        delete(key);
        //last update(put)
        put(newKey, po);
    }
}
