package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.rs.core.dao.po.BizTypePO;

/**
 * @author tangfashuang
 */
public class BizTypeRocksDao extends RocksBaseDao<String, BizTypePO>{
    @Override protected String getColumnFamilyName() {
        return "bizType";
    }
}
