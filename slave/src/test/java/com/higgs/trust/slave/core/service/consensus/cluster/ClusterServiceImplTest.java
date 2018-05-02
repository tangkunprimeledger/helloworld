package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.consensus.bft.core.ConsensusClient;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import com.higgs.trust.slave.common.config.NodeProperties;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.ValidBlockHeaderCmd;
import com.higgs.trust.slave.model.bo.consensus.ValidClusterHeightCmd;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Slf4j @RunWith(PowerMockRunner.class) public class ClusterServiceImplTest {

    @Mock ConsensusClient client;

    @Mock NodeState nodeState;

    NodeProperties properties;

    @InjectMocks ClusterServiceImpl clusterService;

    @BeforeMethod public void before() {
        properties = mock(NodeProperties.class);
        ClusterInfo clusterInfo = mock(ClusterInfo.class);
        P2pConsensusClient p2pConsensusClient = mock(P2pConsensusClient.class);
        clusterService = new ClusterServiceImpl(clusterInfo, p2pConsensusClient, properties);
        MockitoAnnotations.initMocks(this);
    }

    @Test public void testReleaseResult() throws InterruptedException {
        when(properties.getConsensusKeepTime()).thenReturn(0L);
        clusterService.getClusterHeight(1, 0);
        Thread.sleep(1L);
        ConcurrentHashMap<String, ClusterServiceImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, ClusterServiceImpl.ResultListen>)Whitebox
                .getInternalState(clusterService, "resultListenMap");
        assertTrue(resultListenMap.size() == 1);
        clusterService.releaseResult();
        assertTrue(resultListenMap.isEmpty());
    }

    @Test public void testHandleClusterHeight() {
        ValidClusterHeightCmd cmd = new ValidClusterHeightCmd();
        cmd.setRequestId("test");
        ValidCommit<ValidClusterHeightCmd> commit = mock(ValidCommit.class);
        when(commit.operation()).thenReturn(cmd);
        clusterService.handleClusterHeight(commit);
        verify(commit).close();
        ConcurrentHashMap<String, ClusterServiceImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, ClusterServiceImpl.ResultListen>)Whitebox
                .getInternalState(clusterService, "resultListenMap");
        assertTrue(resultListenMap.size() == 0);
    }

    @Test public void testHandleValidHeader() {
        BlockHeader header = mock(BlockHeader.class);
        String blockHash = "blockHash";
        when(header.getBlockHash()).thenReturn(blockHash);
        clusterService.validatingHeader(header, 0L);
        ValidBlockHeaderCmd cmd = new ValidBlockHeaderCmd();
        cmd.setRequestId(blockHash);
        ValidCommit<ValidBlockHeaderCmd> commit = mock(ValidCommit.class);
        when(commit.operation()).thenReturn(cmd);
        clusterService.handleValidHeader(commit);
        verify(commit).close();
        ConcurrentHashMap<String, ClusterServiceImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, ClusterServiceImpl.ResultListen>)Whitebox
                .getInternalState(clusterService, "resultListenMap");
        assertTrue(resultListenMap.size() == 1);
        ClusterServiceImpl.ResultListen resultListen = resultListenMap.get(blockHash);
        assertEquals(resultListen.getResult(), cmd.get());
    }

    @Test public void testGetClusterHeight() {
        when(properties.getConsensusKeepTime()).thenReturn(100L);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Long height = 1L;
        executorService.submit(() -> {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ValidClusterHeightCmd cmd = new ValidClusterHeightCmd("cluster_height_id", height);
            ValidCommit<ValidClusterHeightCmd> commit = ValidCommit.of(ReceiveCommandStatistics.create(cmd));
            clusterService.handleClusterHeight(commit);
        });
        Long clusterHeight = clusterService.getClusterHeight(1, 100L);
        assertEquals(clusterHeight, height);
    }

    @Test public void testGetClusterHeightNoCache() {
        when(properties.getConsensusKeepTime()).thenReturn(0L);
        Long clusterHeight = clusterService.getClusterHeight(1, 0L);
        assertNull(clusterHeight);
        ConcurrentHashMap<String, ClusterServiceImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, ClusterServiceImpl.ResultListen>)Whitebox
                .getInternalState(clusterService, "resultListenMap");
        assertTrue(resultListenMap.size() == 1);
        ClusterServiceImpl.ResultListen resultListen = resultListenMap.get("cluster_height_id");
        assertNotNull(resultListen);
    }

    @Test public void testGetClusterHeightWithCache() {
        ConcurrentHashMap<String, ClusterServiceImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, ClusterServiceImpl.ResultListen>)Whitebox
                .getInternalState(clusterService, "resultListenMap");
        ClusterServiceImpl.ResultListen value = new ClusterServiceImpl.ResultListen(10);
        Long height = new Long(1L);
        value.setResult(height);
        resultListenMap.put("cluster_height_id", value);
        Long clusterHeight = clusterService.getClusterHeight(1, 0L);
        assertNotNull(clusterHeight);
        assertEquals(clusterHeight, height);
    }

    @Test public void testValidatingHeader() {
        when(properties.getConsensusKeepTime()).thenReturn(100L);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        BlockHeader header = mock(BlockHeader.class);
        String blockHash = "blockHash";
        when(header.getBlockHash()).thenReturn(blockHash);
        executorService.submit(() -> {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ValidBlockHeaderCmd cmd = new ValidBlockHeaderCmd(header, false);
            ValidCommit<ValidBlockHeaderCmd> commit = ValidCommit.of(ReceiveCommandStatistics.create(cmd));
            clusterService.handleValidHeader(commit);
        });
        Boolean validateResult = clusterService.validatingHeader(header, 100L);
        assertFalse(validateResult);
    }

    @Test public void testValidatingHeaderNoCache() {
        String blockHash = "blockHash";
        BlockHeader header = mock(BlockHeader.class);
        when(header.getBlockHash()).thenReturn(blockHash);
        Boolean result = clusterService.validatingHeader(header, 0);
        assertNull(result);
        ConcurrentHashMap<String, ClusterServiceImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, ClusterServiceImpl.ResultListen>)Whitebox
                .getInternalState(clusterService, "resultListenMap");
        assertTrue(resultListenMap.size() == 1);
        ClusterServiceImpl.ResultListen resultListen = resultListenMap.get(blockHash);
        assertNotNull(resultListen);
    }

    @Test public void testValidatingHeaderWithCache() {
        String blockHash = "blockHash";
        BlockHeader header = mock(BlockHeader.class);
        when(header.getBlockHash()).thenReturn(blockHash);
        ConcurrentHashMap<String, ClusterServiceImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, ClusterServiceImpl.ResultListen>)Whitebox
                .getInternalState(clusterService, "resultListenMap");
        ClusterServiceImpl.ResultListen value = new ClusterServiceImpl.ResultListen(10);
        value.setResult(Boolean.FALSE);
        resultListenMap.put(blockHash, value);
        Boolean result = clusterService.validatingHeader(header, 0);
        assertFalse(result);
        assertTrue(resultListenMap.size() == 1);
        ClusterServiceImpl.ResultListen resultListen = resultListenMap.get(blockHash);
        assertFalse((Boolean)resultListen.getResult());
    }

}