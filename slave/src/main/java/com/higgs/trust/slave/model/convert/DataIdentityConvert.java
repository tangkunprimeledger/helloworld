package com.higgs.trust.slave.model.convert;

import com.higgs.trust.slave.model.bo.DataIdentity;

import java.util.Date;

/**
 * Data identity convert
 *
 * @author lingchao
 * @create 2018年04月17日20:32
 */
public class DataIdentityConvert {

    /**
     * build a data identity bo
     *
     * @param identity
     * @param chainOwner
     * @param dataOwner
     * @return
     */
    public static  DataIdentity buildDataIdentity(String identity, String chainOwner, String dataOwner){
        DataIdentity dataIdentity = new DataIdentity();
        dataIdentity.setIdentity(identity);
        dataIdentity.setChainOwner(chainOwner);
        dataIdentity.setDataOwner(dataOwner);
        dataIdentity.setCreateTime(new Date());
        return dataIdentity;
    }
}
