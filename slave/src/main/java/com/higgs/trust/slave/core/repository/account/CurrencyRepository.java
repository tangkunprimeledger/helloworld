package com.higgs.trust.slave.core.repository.account;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.dao.mysql.account.AccountJDBCDao;
import com.higgs.trust.slave.dao.mysql.account.CurrencyInfoDao;
import com.higgs.trust.slave.dao.po.account.CurrencyInfoPO;
import com.higgs.trust.slave.dao.rocks.account.CurrencyInfoRocksDao;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-10
 */
@Repository @Slf4j public class CurrencyRepository {
    @Autowired CurrencyInfoDao currencyInfoDao;
    @Autowired AccountJDBCDao accountJDBCDao;
    @Autowired CurrencyInfoRocksDao currencyInfoRocksDao;
    @Autowired InitConfig initConfig;

    /**
     * query currency info
     *
     * @param currency
     * @return
     */
    public CurrencyInfo queryByCurrency(String currency) {
        CurrencyInfoPO currencyInfoPO;
        if (initConfig.isUseMySQL()) {
            currencyInfoPO = currencyInfoDao.queryByCurrency(currency);
        } else {
            currencyInfoPO = currencyInfoRocksDao.get(currency);
        }
        return BeanConvertor.convertBean(currencyInfoPO, CurrencyInfo.class);
    }

    /**
     * check currency info
     *
     * @param currency
     * @return
     */
    public boolean isExits(String currency) {
        if(StringUtils.isEmpty(currency)){
            return false;
        }
        if (initConfig.isUseMySQL()) {
            return currencyInfoDao.queryByCurrency(currency) != null;
        }
        return currencyInfoRocksDao.get(currency) != null;
    }

    /**
     * build an new currency info
     *
     * @param currency
     * @param remark
     * @return
     */
    public CurrencyInfo buildCurrencyInfo(String currency, String remark) {
        CurrencyInfo currencyInfo = new CurrencyInfo();
        currencyInfo.setCurrency(currency);
        currencyInfo.setRemark(remark);
        currencyInfo.setCreateTime(new Date());
        return currencyInfo;
    }

    /**
     * batch insert
     *
     * @param currencyInfos
     */
    public void batchInsert(List<CurrencyInfo> currencyInfos) {
        if (CollectionUtils.isEmpty(currencyInfos)) {
            return;
        }
        try {
            Profiler.enter("[batchInsert currencyInfo]");

            if (initConfig.isUseMySQL()) {
                int r = accountJDBCDao.batchInsertCurrency(currencyInfos);
                if (r != currencyInfos.size()) {
                    log.info("[batchInsert]the number of update rows is different from the original number");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_BATCH_INSERT_ROWS_DIFFERENT_ERROR);
                }
            } else {
                currencyInfoRocksDao.batchInsert(
                    BeanConvertor.convertList(currencyInfos, CurrencyInfoPO.class));
            }

        } catch (DuplicateKeyException e) {
            log.error("[batchInsert] has idempotent for currencyInfos:{}", currencyInfos);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        } finally {
            Profiler.release();
        }
    }
}
