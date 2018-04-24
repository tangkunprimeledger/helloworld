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
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author suimi
 * @date 2018/4/23
 */
@Service @Slf4j public class ClusterServiceImpl extends ValidConsensus implements ClusterService {

    @Autowired ConsensusClient client;

    @Autowired NodeProperties properties;

    @Autowired NodeState nodeState;

    private ConcurrentHashMap<String, ResultListen> resultListenMap = new ConcurrentHashMap<>();

    private static final String DEFAULT_CLUSTER_HEIGHT_ID = "cluster_height_id";

    public ClusterServiceImpl(ClusterInfo clusterInfo, P2pConsensusClient client, NodeProperties properties) {
        super(clusterInfo, client, properties.getConsensusDir());
    }

    @Scheduled(fixedDelay = 30000) public void releaseResult() {
        for (Map.Entry<String, ResultListen> entry : resultListenMap.entrySet()) {
            if (entry.getValue().isTimeout()) {
                resultListenMap.remove(entry.getKey());
            }
        }
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
        return getClusterHeight(DEFAULT_CLUSTER_HEIGHT_ID, size, time);
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
        ResultListen resultListen = resultListenMap.get(requestId);
        if (resultListen != null && resultListen.getResult() != null) {
            return (Long)resultListen.getResult();
        }
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
        String blockHash = header.getBlockHash();
        ResultListen resultListen = resultListenMap.get(blockHash);
        if (resultListen != null && resultListen.getResult() != null) {
            return (Boolean)resultListen.getResult();
        }
        client.submit(new BlockHeaderCmd(nodeState.getNodeName(), header));
        return registerAndGetResult(blockHash, time);
    }

    private <T> T registerAndGetResult(String requestId, long time) {
        ResultListen resultListen = register(requestId, properties.getConsensusKeepTime());
        try {
            resultListen.getLatch().await(time, TimeUnit.MILLISECONDS);
            return getResult(requestId);
        } catch (InterruptedException e) {
            log.warn("waiting the consensus result failed", e);
            return null;
        }
    }

    private ResultListen register(String requestId, long time) {
        ResultListen resultListen = resultListenMap.getOrDefault(requestId, new ResultListen(time));
        resultListen.renew();
        resultListenMap.put(requestId, resultListen);
        return resultListen;
    }

    private <T> T getResult(String id) {
        ResultListen resultListen = resultListenMap.get(id);
        if (resultListen == null) {
            return null;
        }
        Object result = resultListen.getResult();
        if (result == null) {
            return null;
        }
        return (T)result;
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

    protected static class ResultListen {

        /**
         * keep time
         */
        private long keepTime;

        /**
         * register time
         */
        private long registerTime = System.currentTimeMillis();
        @Getter private CountDownLatch latch = new CountDownLatch(1);

        /**
         * result
         */
        @Getter @Setter private Object result;

        protected ResultListen(long keepTime) {
            this.keepTime = keepTime;
        }

        public void renew() {
            registerTime = System.currentTimeMillis();
            latch = new CountDownLatch(1);
        }

        public boolean isTimeout() {
            return registerTime + keepTime < System.currentTimeMillis();
        }

    }
}
