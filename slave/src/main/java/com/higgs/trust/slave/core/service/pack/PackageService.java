package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;

import java.util.List;

/**
 * @Description:package services include build, validate, persist
 * @author: pengdi
 **/
public interface PackageService {
    /**
     * create new package from pending transactions
     *
     * @param signedTransactions
     * @param currentPackageHeight
     * @return
     */
    Package create(List<SignedTransaction> signedTransactions, Long currentPackageHeight);

    /**
     * package status change function
     *
     * @param pack
     * @param from
     * @param to
     */
    void statusChange(Package pack, PackageStatusEnum from, PackageStatusEnum to);

    /**
     * receive new package from somewhere, almost from consensus
     *
     * @param pack
     */
    void receive(Package pack);

    /**
     * create pack context for main process
     *
     * @param pack
     * @return
     */
    PackContext createPackContext(Package pack);

    /**
     * execute package persisting
     *
     * @param packContext
     * @param isFailover
     */
    void process(PackContext packContext,boolean isFailover);

    /**
     * remove the package if it is done
     *
     * @param pack
     */
    void remove(Package pack);

    /**
     * submit package to consensus
     * @param pack
     */
    void submitConsensus(Package pack);

    /**
     * persisted
     * @param header
     */
    void persisted(BlockHeader header);
}
