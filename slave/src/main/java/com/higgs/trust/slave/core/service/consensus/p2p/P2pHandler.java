package com.higgs.trust.slave.core.service.consensus.p2p;

import com.higgs.trust.slave.model.bo.BlockHeader;

/**
 * @Description: p2p interface for package validate and persist
 * @author: pengdi
 **/
public interface P2pHandler {

    /**
     * send validating result to p2p consensus layer
     *
     * @param header
     */
    void sendPersisting(BlockHeader header);
}
