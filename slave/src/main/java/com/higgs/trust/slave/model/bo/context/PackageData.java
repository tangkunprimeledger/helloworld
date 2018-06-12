package com.higgs.trust.slave.model.bo.context;

import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;

import java.util.Map;

/**
 * @Description:
 * @author: pengdi
 **/
public interface PackageData extends CommonData {

    /**
     * set current transaction
     *
     * @return
     */
    void setCurrentTransaction(SignedTransaction transaction);

    /**
     * parse context transaction data in this package processing
     * use parse no get for JSON
     *
     * @return
     */
    TransactionData parseTransactionData();

    /**
     * set the package
     *
     * @param currentPackage
     */
    void setCurrentPackage(Package currentPackage);

    /**
     * set the block
     *
     * @param block
     */
    void setCurrentBlock(Block block);

    void setRsPubKeyMap(Map<String, String> rsPubKeyMap);

    Map<String, String> getRsPubKeyMap();
}
