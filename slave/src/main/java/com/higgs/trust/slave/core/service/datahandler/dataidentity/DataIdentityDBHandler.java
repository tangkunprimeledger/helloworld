package com.higgs.trust.slave.core.service.datahandler.dataidentity;

import com.higgs.trust.slave.core.repository.DataIdentityRepository;
import com.higgs.trust.slave.model.bo.DataIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * DataIdentity DB Handler
 *
 * @author lingchao
 * @create 2018年04月17日19:42
 */
@Service
public class DataIdentityDBHandler implements DataIdentityHandler{
    @Autowired
    private DataIdentityRepository dataIdentityRepository;

    /**
     * get dataIdentity by identity
     * @param identity
     * @return
     */
    @Override
    public DataIdentity getDataIdentity(String identity){
     return dataIdentityRepository.queryDataIdentity(identity);
    }

    /**
     * save dataIdentity
     * @param dataIdentity
     * @return
     */
    @Override
    public void saveDataIdentity(DataIdentity dataIdentity) { dataIdentityRepository.save(dataIdentity); }


}
