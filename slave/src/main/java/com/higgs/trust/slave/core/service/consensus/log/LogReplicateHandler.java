package com.higgs.trust.slave.core.service.consensus.log;

import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterVerifyResponse;

import java.util.Map;

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

    /**
     * change master
     *
     * @param term
     * @param verifies
     */
    void changeMaster(long term, Map<String, ChangeMasterVerifyResponse> verifies);

    /**
     * master heartbeat
     */
    void masterHeartbeat();

}
