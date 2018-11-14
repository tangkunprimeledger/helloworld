package com.higgs.trust.slave.core.repository.contract;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.dao.mysql.contract.ContractDao;
import com.higgs.trust.slave.dao.po.contract.ContractPO;
import com.higgs.trust.slave.dao.rocks.contract.ContractRocksDao;
import com.higgs.trust.slave.model.bo.contract.Contract;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Contract Repository
 * @author duhongming
 * @date 2018-04-12
 */
@Repository
@Slf4j
public class ContractRepository {

    @Autowired private ContractDao contractDao;
    @Autowired private ContractRocksDao contractRocksDao;
    @Autowired private InitConfig initConfig;

    public boolean batchInsert(List<ContractPO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }

        int result;
        if (initConfig.isUseMySQL()) {
            result = contractDao.batchInsert(list);
        } else {
            result = contractRocksDao.batchInsert(list);
        }
        return result == list.size();
    }

    public Long queryCount(Long height, String txId) {
        Long rowCount = contractDao.getQueryCount(height, txId);
        return rowCount;
    }

    public List<Contract> query(Long height, String txId, int pageIndex, int pageSize) {
        List<ContractPO> list = contractDao.query(height, txId, (pageIndex - 1) * pageSize, pageSize);
        return BeanConvertor.convertList(list, Contract.class);
    }

    public Contract queryByAddress(String address) {

        ContractPO po;
        if (initConfig.isUseMySQL()) {
            po = contractDao.queryByAddress(address);
        } else {
            po = contractRocksDao.get(address);
        }
        return BeanConvertor.convertBean(po, Contract.class);
    }

    public boolean isExistedAddress(String address){
        if (StringUtils.isBlank(address)) {
            return false;
        }
        Contract contract = queryByAddress(address);
        if (null ==  contract){
            return false;
        }
        return true;
    }
}