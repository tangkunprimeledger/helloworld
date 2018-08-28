package com.higgs.trust.slave.dao.rocks.account;

import com.google.common.collect.Lists;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.account.CurrencyInfoPO;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class CurrencyInfoRocksDao extends RocksBaseDao<String, CurrencyInfoPO> {

    @Override protected String getColumnFamilyName() {
        return "currencyInfo";
    }

    public void batchInsert(List<CurrencyInfoPO> currencyInfos) {
        if (CollectionUtils.isEmpty(currencyInfos)) {
            return;
        }

        for (CurrencyInfoPO currencyInfo : currencyInfos) {
            currencyInfo.setCreateTime(new Date());
            batchPut(ThreadLocalUtils.getWriteBatch(), currencyInfo.getCurrency(), currencyInfo);
        }
    }
}
