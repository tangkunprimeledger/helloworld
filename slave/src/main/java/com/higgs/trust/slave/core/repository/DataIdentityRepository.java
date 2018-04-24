package com.higgs.trust.slave.core.repository;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.DataIdentityDao;
import com.higgs.trust.slave.dao.po.DataIdentityPO;
import com.higgs.trust.slave.model.bo.DataIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;



/**
 * @author liuyu
 * @description
 * @date 2018-04-10
 */
@Repository @Slf4j public class DataIdentityRepository {
    @Autowired
    private DataIdentityDao dataIdentityDao;
    /**
     * query identity data by identity
     *
     * @param identity
     * @return
     */
    public DataIdentity queryDataIdentity(String identity) {
        DataIdentityPO identityPO = dataIdentityDao.queryByIdentity(identity);
        return BeanConvertor.convertBean(identityPO, DataIdentity.class);
    }

    /**
     * save data identity
     * @param dataIdentity
     */
    public void save(DataIdentity dataIdentity){
        DataIdentityPO dataIdentityPO = BeanConvertor.convertBean(dataIdentity,DataIdentityPO.class);
        try {
            dataIdentityDao.add(dataIdentityPO);
        } catch (DuplicateKeyException e) {
            log.error("Insert dataIdentityPO fail, because there is DuplicateKeyException for dataidentity:",
                dataIdentityPO);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT, e);
        }
    }
}
