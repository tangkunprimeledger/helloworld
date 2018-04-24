package com.higgs.trust.slave.api;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.SignedTransaction;

import java.util.List;

/**
 *
 * @author pengdi
 * @date 
 */
public interface BlockChainService {

    /**
     * create transactions
     *
     * @param transactions
     * @return
     */
    RespData submitTransaction(List<SignedTransaction> transactions);
}
