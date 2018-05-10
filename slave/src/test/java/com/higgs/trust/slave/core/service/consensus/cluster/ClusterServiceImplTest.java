package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.consensus.bft.core.ConsensusClient;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.slave.common.config.NodeProperties;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.service.consensus.p2p.P2pHandlerImpl;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.ValidBlockHeaderCmd;
import com.higgs.trust.slave.model.bo.consensus.ValidClusterHeightCmd;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Slf4j @RunWith(PowerMockRunner.class) @PrepareForTest(P2pHandlerImpl.class) public class ClusterServiceImplTest {

    @Mock ConsensusClient client;

    @Mock NodeState nodeState;

    @InjectMocks P2pHandlerImpl p2pHandler;

    @BeforeMethod public void before() {
        p2pHandler = new P2pHandlerImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test public void testReleaseResult() throws InterruptedException {
        p2pHandler.getClusterHeight("cluster_height_id", 1, 0);
        Thread.sleep(1L);
        ConcurrentHashMap<String, P2pHandlerImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, P2pHandlerImpl.ResultListen>)Whitebox
                .getInternalState(p2pHandler, "resultListenMap");
        assertEquals(resultListenMap.size(), 0);
        assertTrue(resultListenMap.isEmpty());
    }

    @Test public void testHandleClusterHeight() {
        ValidClusterHeightCmd cmd = new ValidClusterHeightCmd();
        cmd.setRequestId("test");
        ValidCommit<ValidClusterHeightCmd> commit = mock(ValidCommit.class);
        when(commit.operation()).thenReturn(cmd);
        p2pHandler.handleClusterHeight(commit);
        verify(commit).close();
        ConcurrentHashMap<String, P2pHandlerImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, P2pHandlerImpl.ResultListen>)Whitebox
                .getInternalState(p2pHandler, "resultListenMap");
        assertTrue(resultListenMap.size() == 0);
    }

    @Test public void testHandleValidHeader() {
        BlockHeader header = mock(BlockHeader.class);
        String blockHash = "blockHash";
        when(header.getBlockHash()).thenReturn(blockHash);
        p2pHandler.validatingHeader(header, 0L);
        ValidBlockHeaderCmd cmd = new ValidBlockHeaderCmd();
        cmd.setRequestId(blockHash);
        ValidCommit<ValidBlockHeaderCmd> commit = mock(ValidCommit.class);
        when(commit.operation()).thenReturn(cmd);
        p2pHandler.handleValidHeader(commit);
        verify(commit).close();
        ConcurrentHashMap<String, P2pHandlerImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, P2pHandlerImpl.ResultListen>)Whitebox
                .getInternalState(p2pHandler, "resultListenMap");
        assertEquals(resultListenMap.size(), 0);
    }

    @Test public void testGetClusterHeight() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Long height = 1L;
        executorService.submit(() -> {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ValidClusterHeightCmd cmd = new ValidClusterHeightCmd("cluster_height_id", height);
//            ValidCommit<ValidClusterHeightCmd> commit = ValidCommit.of(ReceiveCommandStatistics.create(cmd));
//            p2pHandler.handleClusterHeight(commit);
        });
        Long clusterHeight = p2pHandler.getClusterHeight("cluster_height_id", 1, 100L);
        assertEquals(clusterHeight, height);
    }

    @Test public void testGetClusterHeightNoCache() {
        Long clusterHeight = p2pHandler.getClusterHeight("cluster_height_id", 1, 0L);
        assertNull(clusterHeight);
        ConcurrentHashMap<String, P2pHandlerImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, P2pHandlerImpl.ResultListen>)Whitebox
                .getInternalState(p2pHandler, "resultListenMap");
        assertEquals(resultListenMap.size(), 0);
    }

    @Test public void testGetClusterHeightWithCache() {
        ConcurrentHashMap<String, P2pHandlerImpl.ResultListen> resultListenMap =
            (ConcurrentHashMap<String, P2pHandlerImpl.ResultListen>)Whitebox
                .getInternalState(p2pHandler, "resultListenMap");
        P2pHandlerImpl.ResultListen value = new P2pHandlerImpl.ResultListen();
        Long height = new Long(1L);
        value.setResult(height);
        resultListenMap.put("cluster_height_id", value);
        Long clusterHeight = p2pHandler.getClusterHeight("cluster_height_id", 1, 0L);
        assertNotNull(clusterHeight);
        assertEquals(clusterHeight, height);
    }

    @Test public void testValidatingHeader() {
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.currentTimeMillis()).thenReturn(new Long(20180503L).longValue());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        BlockHeader header = mock(BlockHeader.class);
        when(header.getHeight()).thenReturn(1L);
        executorService.submit(() -> {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ValidBlockHeaderCmd cmd = new ValidBlockHeaderCmd("valid_block_header_1_20180503", header, false);
//            ValidCommit<ValidBlockHeaderCmd> commit = ValidCommit.of(ReceiveCommandStatistics.create(cmd));
//            p2pHandler.handleValidHeader(commit);
        });
        Boolean validateResult = p2pHandler.validatingHeader(header, 100L);
        assertFalse(validateResult);
    }

}