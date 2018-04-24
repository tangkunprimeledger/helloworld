package com.higgs.trust.slave.model.bo.context;

import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;

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
     * handle next transaction in this package processing
     *
     * @return
     */
    TransactionData getTransactionData();

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
}
