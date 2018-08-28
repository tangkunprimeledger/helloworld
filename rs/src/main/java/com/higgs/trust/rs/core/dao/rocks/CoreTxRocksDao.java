package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class CoreTxRocksDao extends RocksBaseDao<String, CoreTransactionPO>{
    @Override protected String getColumnFamilyName() {
        return "coreTransaction";
    }
}
