/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.slave.model.bo.BlockHeader;

import java.util.concurrent.TimeUnit;

public interface ClusterService {

    /**
     * get the block height of cluster
     *
     * @param size    the size of height will be consensus
     * @param timeout waiting time for the result
     * @return
     */
    Long getClusterHeight(int size, long timeout);

    /**
     * get the block height of cluster
     *
     * @param requestId the id of request
     * @param size      the size of height will be consensus
     * @param timeout   waiting time for the result
     * @return
     */
    Long getClusterHeight(String requestId, int size, long timeout);

    /**
     * cluster validates the block header
     *
     * @param header  block header
     * @param timeout timeout  for waiting result
     * @return
     */
    Boolean validatingHeader(BlockHeader header, long timeout);

}
