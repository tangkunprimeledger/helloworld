package com.higgs.trust.slave.dao.rocks.config;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.slave.dao.po.config.SystemPropertyPO;
import org.springframework.stereotype.Service;

/**
 * @author tangfashuang
 */
@Service
public class SystemPropertyRocksDao extends RocksBaseDao<SystemPropertyPO> {
    @Override protected String getColumnFamilyName() {
        return "systemProperty";
    }
}