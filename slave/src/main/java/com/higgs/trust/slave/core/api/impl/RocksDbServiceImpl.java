package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.slave.api.RocksDbService;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class RocksDbServiceImpl implements RocksDbService {
    @Override public RespData getData(String columnFamily, String key) {
        RespData respData = new RespData();
        respData.setData(RocksUtils.getData(columnFamily, key));
        return respData;
    }
}
