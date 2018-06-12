package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.api.vo.PackageVO;
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
     * @return
     */
    Package create(List<SignedTransaction> signedTransactions, Long packHeight);

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
     */
    void process(PackContext packContext);

    /**
     * remove the package if it is done
     *
     * @param pack
     */
    void remove(Package pack);

    /**
     * get package signature
     *
     * @param packageVO
     * @return
     */
    String getSign(PackageVO packageVO);

    /**
     * submit package to consensus
     * @param pack
     */
    void submitConsensus(Package pack);

<<<<<<< HEAD
=======
    /**
     * persisted
     * @param header
     */
    void persisted(BlockHeader header);
>>>>>>> dev_0610_ca
}
