package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;

/**
 * @Description:package services include build, validate, persist
 * @author: pengdi
 **/
public interface PackageService {
    /**
     * create new package from pending transactions
     *
     * @return
     */
    Package create();

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
     * execute package validating
     *
     * @param packContext
     */
    void validating(PackContext packContext);

    /**
     * get validate result and submit consensus layer
     *
     * @param pack
     */
    void validateConsensus(Package pack);

    /**
     * receive validate final result from consensus layer
     *
     * @param pack
     */
    void validated(Package pack);

    /**
     * execute package persisting
     *
     * @param packContext
     */
    void persisting(PackContext packContext);

    /**
     * get persist result and submit consensus layer
     *
     * @param pack
     */
    void persistConsensus(Package pack);

    /**
     * receive persist final result from consensus layer
     *
     * @param pack
     */
    void persisted(Package pack);

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
}
