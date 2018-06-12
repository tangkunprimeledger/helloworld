package com.higgs.trust.slave.core.service.failover;

import com.higgs.trust.config.node.NodeProperties;
import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@RunWith(PowerMockRunner.class) public class SelfCheckingServiceTest {

    @Autowired @InjectMocks SelfCheckingService selfCheckingService;

    @Mock NodeState nodeState;
    @Mock BlockSyncService blockSyncService;
    @Mock BlockService blockService;
    @Mock BlockRepository blockRepository;

    @Mock Block block;
    @Mock BlockHeader header;
    @Mock NodeProperties properties;

    @BeforeClass public void before() {
        MockitoAnnotations.initMocks(this);
        long height = 1L;
        when(blockService.getMaxHeight()).thenReturn(height);
        when(block.getBlockHeader()).thenReturn(header);
        when(blockRepository.getBlock(height)).thenReturn(block);
        when(properties.getSelfCheckTimes()).thenReturn(1);
    }

    @BeforeMethod public void beforeMethod() {
        reset(nodeState);
    }

    @Test public void testCheckTrueNotMaster() {

        when(nodeState.isMaster()).thenReturn(false);
        when(blockSyncService.validating(block)).thenReturn(true);
        when(blockSyncService.bftValidating(header)).thenReturn(true);

        assertTrue(selfCheckingService.check());
        verify(nodeState, times(1)).changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        verify(nodeState, times(1)).changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
    }

    @Test public void testCheckTrueNotMaster1() {
        when(nodeState.isMaster()).thenReturn(false);

        when(blockSyncService.validating(block)).thenReturn(true);
        when(blockSyncService.bftValidating(header)).thenReturn(null, true);
        when(properties.getSelfCheckTimes()).thenReturn(2);

        assertTrue(selfCheckingService.check());
        verify(nodeState, times(1)).changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        verify(nodeState, times(1)).changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
    }

    @Test public void testCheckFalseNotMaster() {
        when(nodeState.isMaster()).thenReturn(false);

        when(blockSyncService.validating(block)).thenReturn(false);

        assertFalse(selfCheckingService.check());
        verify(nodeState, times(1)).changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        verify(nodeState, times(1)).changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Offline);
    }

    @Test public void testCheckFalseNotMaster2() {
        when(nodeState.isMaster()).thenReturn(false);

        when(blockSyncService.validating(block)).thenReturn(true);
        when(blockSyncService.bftValidating(header)).thenReturn(null, false);

        assertFalse(selfCheckingService.check());
        verify(nodeState, times(1)).changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        verify(nodeState, times(1)).changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Offline);
    }

    //    @Test
    public void testCheckBftValidOut() {
        when(nodeState.isMaster()).thenReturn(false);

        when(blockSyncService.validating(block)).thenReturn(true);
        when(blockSyncService.bftValidating(header)).thenReturn(null);

        assertFalse(selfCheckingService.check());
        verify(nodeState, times(1)).changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        verify(nodeState, times(1)).changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Offline);
    }

    @Test public void testMasterCheck() {
    }
}