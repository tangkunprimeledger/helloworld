/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.consensus.bft.core.ConsensusClient;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.slave.common.config.NodeProperties;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author suimi
 * @date 2018/4/23
 */
@Service @Slf4j public class ClusterServiceImpl extends ValidConsensus implements ClusterService {

    NodeProperties properties;

    @Autowired ConsensusClient client;

    @Autowired NodeState nodeState;

    private ConcurrentHashMap<String, ResultListen> resultListenMap = new ConcurrentHashMap<>();

    private static final String DEFAULT_CLUSTER_HEIGHT_ID = "cluster_height_id";

    @Autowired
    public ClusterServiceImpl(ClusterInfo clusterInfo, P2pConsensusClient client, NodeProperties properties) {
        this.properties = properties;
    }

    /**
     * handle the consensus result of cluster height
     *
     * @param commit
     */
    public void handleClusterHeight(ValidCommit<ValidClusterHeightCmd> commit) {
        handleResult(commit);
    }

    /**
     * handle the consensus result of validating block header
     *
     * @param commit
     */
    public void handleValidHeader(ValidCommit<ValidBlockHeaderCmd> commit) {
        handleResult(commit);
    }

    /**
     * get the cluster height through consensus, the default request id will be set. if timeout, null will be return
     *
     * @param size the size of height will be consensus
     * @param time waiting time for the result
     * @return
     */
    @Override public Long getClusterHeight(int size, long time) {
        return getClusterHeight(DEFAULT_CLUSTER_HEIGHT_ID + Constant.SPLIT_SLASH + System.currentTimeMillis(), size,
            time);
    }

    /**
     * get the cluster height through consensus, if timeout, null will be return
     *
     * @param requestId the id of request
     * @param size      the size of height will be consensus
     * @param time      waiting time for the result
     * @return
     */
    @Override public Long getClusterHeight(String requestId, int size, long time) {
        client.submit(new ClusterHeightCmd(requestId, nodeState.getNodeName(), size));
        return registerAndGetResult(requestId, time);
    }

    /**
     * validating the block header through consensus, if timeout, null will be return
     *
     * @param header block header
     * @param time   waiting time for the result
     * @return
     */
    @Override public Boolean validatingHeader(BlockHeader header, long time) {
        BlockHeaderCmd command = new BlockHeaderCmd(nodeState.getNodeName(), header);
        client.submit(command);
        return registerAndGetResult(command.getRequestId(), time);
    }

    private <T> T registerAndGetResult(String requestId, long time) {
        try {
            ResultListen resultListen = register(requestId);
            resultListen.getLatch().await(time, TimeUnit.MILLISECONDS);
            return getResult(requestId);
        } catch (InterruptedException e) {
            log.warn("waiting the consensus result failed", e);
            return null;
        } finally {
            resultListenMap.remove(requestId);
        }
    }

    private ResultListen register(String requestId) {
        ResultListen resultListen = resultListenMap.getOrDefault(requestId, new ResultListen());
        resultListen.renew();
        resultListenMap.put(requestId, resultListen);
        return resultListen;
    }

    private <T> T getResult(String id) {
        ResultListen resultListen = resultListenMap.get(id);
        Object result = resultListen.getResult();
        return result == null ? null : (T)result;
    }

    private <T extends Serializable, C extends IdValidCommand<T>> void handleResult(ValidCommit<C> commit) {
        if (log.isDebugEnabled()) {
            log.debug("handle consensus result:{}", ToStringBuilder.reflectionToString(commit));
        }
        try {
            IdValidCommand<T> command = commit.operation();
            ResultListen resultListen = resultListenMap.get(command.getRequestId());
            if (resultListen != null) {
                resultListen.setResult(command.get());
                resultListen.getLatch().countDown();
            }
        } finally {
            commit.close();
        }
    }

    static class ResultListen {

        @Getter private CountDownLatch latch = new CountDownLatch(1);

        /**
         * result
         */
        @Getter @Setter private Object result;

        public void renew() {
            latch = new CountDownLatch(1);
        }
    }
}
