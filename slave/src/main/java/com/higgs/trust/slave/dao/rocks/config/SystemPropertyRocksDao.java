package com.higgs.trust.slave.dao.rocks.config;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.slave.dao.po.config.SystemPropertyPO;

/**
 * @author tangfashuang
 */
public class SystemPropertyRocksDao extends RocksBaseDao<String, SystemPropertyPO> {
    @Override protected String getColumnFamilyName() {
        return "systemProperty";
    }
}