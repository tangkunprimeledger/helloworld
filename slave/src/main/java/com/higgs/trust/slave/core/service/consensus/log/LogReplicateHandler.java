package com.higgs.trust.slave.core.service.consensus.log;

import com.higgs.trust.slave.api.vo.PackageVO;

import java.util.List;

/**
 * @Description: log replicate handler, mostly deal with sorted package
 * @author: pengdi
 **/
public interface LogReplicateHandler {

    /**
     * replicate sorted package to the cluster
     *
     * @param packageVOList
     */
    void replicatePackage(List<PackageVO> packageVOList);
}
