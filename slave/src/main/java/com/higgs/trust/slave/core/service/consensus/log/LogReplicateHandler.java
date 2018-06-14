package com.higgs.trust.slave.core.service.consensus.log;

import com.higgs.trust.slave.api.vo.PackageVO;

/**
 * @Description: log replicate handler, mostly deal with sorted package
 * @author: pengdi
 **/
public interface LogReplicateHandler {

    /**
     * replicate sorted package to the cluster
     *
     * @param packageVO
     */
    void replicatePackage(PackageVO packageVO);
}
