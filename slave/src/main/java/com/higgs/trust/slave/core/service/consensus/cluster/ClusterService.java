/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.slave.model.bo.BlockHeader;

public interface ClusterService {

    /**
     * get the block height of cluster
     *
     * @param size    the size of height will be consensus
     * @return
     */
    Long getClusterHeight(int size);

    /**
     * get the block height of cluster
     *
     * @param requestId the id of request
     * @param size      the size of height will be consensus
     * @return
     */
    Long getClusterHeight(String requestId, int size);

    /**
     * cluster validates the block header
     *
     * @param header  block header
     * @return
     */
    Boolean validatingHeader(BlockHeader header);

}
