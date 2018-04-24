package com.higgs.trust.slave.core.service.datahandler.dataidentity;

import com.higgs.trust.slave.core.service.snapshot.agent.DataIdentitySnapshotAgent;
import com.higgs.trust.slave.model.bo.DataIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DataIdentity  snapshot Handler
 *
 * @author lingchao
 * @create 2018年04月17日19:43
 */
@Service
public class DataIdentitySnapshotHandler implements DataIdentityHandler{
    @Autowired
    private DataIdentitySnapshotAgent dataIdentitySnapshotAgent;
    /**
     * get dataIdentity by identity
     * @param identity
     * @return
     */
    @Override
    public DataIdentity getDataIdentity(String identity){
        return dataIdentitySnapshotAgent.getDataIdentity(identity);
    }

    /**
     * save dataIdentity
     * @param dataIdentity
     * @return
     */
    @Override
    public void saveDataIdentity(DataIdentity dataIdentity){
        dataIdentitySnapshotAgent.saveDataIdentity(dataIdentity);
    }
}
