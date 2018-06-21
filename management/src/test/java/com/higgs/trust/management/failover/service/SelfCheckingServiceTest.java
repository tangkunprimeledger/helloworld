package com.higgs.trust.management.failover.service;

import com.higgs.trust.config.node.NodeProperties;
import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.management.exception.FailoverExecption;
import com.higgs.trust.management.exception.ManagementError;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
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
        Mockito.when(blockService.getMaxHeight()).thenReturn(height);
        Mockito.when(block.getBlockHeader()).thenReturn(header);
        Mockito.when(blockRepository.getBlock(height)).thenReturn(block);
        Mockito.when(properties.getStartupRetryTime()).thenReturn(1);
    }

    @BeforeMethod public void beforeMethod() {
        Mockito.reset(nodeState);
    }

    @Test public void testCheckTrueNotMaster() {

        Mockito.when(nodeState.isMaster()).thenReturn(false);
        Mockito.when(blockSyncService.validating(block)).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(header)).thenReturn(true);

        selfCheckingService.autoCheck();
    }

    @Test public void testCheckTrueNotMaster1() {
        Mockito.when(nodeState.isMaster()).thenReturn(false);

        Mockito.when(blockSyncService.validating(block)).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(header)).thenReturn(null, true);
        Mockito.when(properties.getStartupRetryTime()).thenReturn(2);

        selfCheckingService.autoCheck();
    }

    @Test public void testCheckFalseNotMaster() {
        Mockito.when(nodeState.isMaster()).thenReturn(false);

        Mockito.when(blockSyncService.validating(block)).thenReturn(false);
        try{
            selfCheckingService.autoCheck();
        }catch (FailoverExecption e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_STARTUP_SELF_CHECK_FAILED);
        }
    }

    @Test public void testCheckFalseNotMaster2() {
        Mockito.when(nodeState.isMaster()).thenReturn(false);

        Mockito.when(blockSyncService.validating(block)).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(header)).thenReturn(null, false);

        try{
            selfCheckingService.autoCheck();
        }catch (FailoverExecption e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_STARTUP_SELF_CHECK_FAILED);
        }
    }

    //    @Test
    public void testCheckBftValidOut() {
        Mockito.when(nodeState.isMaster()).thenReturn(false);

        Mockito.when(blockSyncService.validating(block)).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(header)).thenReturn(null);

        try{
            selfCheckingService.autoCheck();
        }catch (FailoverExecption e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_STARTUP_SELF_CHECK_FAILED);
        }
    }
}