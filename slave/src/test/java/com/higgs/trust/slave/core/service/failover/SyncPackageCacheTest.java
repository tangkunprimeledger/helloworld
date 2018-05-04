package com.higgs.trust.slave.core.service.failover;

import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.model.bo.Package;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
 * Copyright (c) 2013-2017, suimi
 *
 */
@RunWith(PowerMockRunner.class) public class SyncPackageCacheTest {

    @Autowired @InjectMocks private SyncPackageCache packageCache;

    @Mock private FailoverProperties failoverProperties;

    @Mock private NodeState nodeState;

    @BeforeClass public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod public void beforMethod() {
        packageCache.clean();
    }

    @Test public void testReceiveNot() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(false);
        packageCache.receivePackageHeight(1L);
        assertEquals(packageCache.getMinHeight(), SyncPackageCache.INIT_HEIGHT);
    }

    @Test public void testReceive() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        packageCache.clean();
        assertEquals(packageCache.getMinHeight(), SyncPackageCache.INIT_HEIGHT);
        assertEquals(packageCache.getLatestHeight(), SyncPackageCache.INIT_HEIGHT);

        when(failoverProperties.getThreshold()).thenReturn(100);
        when(failoverProperties.getKeepSize()).thenReturn(10);
        long height = 1L;
        packageCache.receivePackageHeight(height);
        assertEquals(packageCache.getMinHeight(), height);
        assertEquals(packageCache.getLatestHeight(), height);

        height = 2L;
        packageCache.receivePackageHeight(height);
        assertEquals(packageCache.getMinHeight(), 1L);
        assertEquals(packageCache.getLatestHeight(), height);

        height = 1L;
        packageCache.receivePackageHeight(height);
        assertEquals(packageCache.getMinHeight(), 1L);
        assertEquals(packageCache.getLatestHeight(), 2L);

        height = 3L;
        packageCache.receivePackageHeight(height);
        assertEquals(packageCache.getMinHeight(), 1L);
        assertEquals(packageCache.getLatestHeight(), height);

        height = 5L;
        packageCache.receivePackageHeight(height);
        assertEquals(packageCache.getMinHeight(), height);
        assertEquals(packageCache.getLatestHeight(), height);
    }

    @Test public void testReceiveOutThreshold() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        when(failoverProperties.getThreshold()).thenReturn(100);
        when(failoverProperties.getKeepSize()).thenReturn(10);

        long height = 1L;

        do {
            packageCache.receivePackageHeight(height);
            assertEquals(packageCache.getMinHeight(), 1L);
            assertEquals(packageCache.getLatestHeight(), height);
        } while (++height <= 100);

        packageCache.receivePackageHeight(101L);
        assertEquals(packageCache.getMinHeight(), 91L);
        assertEquals(packageCache.getLatestHeight(), 101L);
    }

    @Test public void testReceiveOutThreshold2() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        when(failoverProperties.getThreshold()).thenReturn(10);
        when(failoverProperties.getKeepSize()).thenReturn(11);

        long height = 1L;

        do {
            packageCache.receivePackageHeight(height);
            assertEquals(packageCache.getMinHeight(), 1L);
            assertEquals(packageCache.getLatestHeight(), height);
        } while (++height <= 10);

        packageCache.receivePackageHeight(11L);
        assertEquals(packageCache.getMinHeight(), 11L);
        assertEquals(packageCache.getLatestHeight(), 11L);

        do {
            packageCache.receivePackageHeight(height);
            assertEquals(packageCache.getMinHeight(), 11L);
            assertEquals(packageCache.getLatestHeight(), height);
        } while (++height <= 20);

        packageCache.receivePackageHeight(21L);
        assertEquals(packageCache.getMinHeight(), 21L);
        assertEquals(packageCache.getLatestHeight(), 21L);
    }

    @Test public void testClean() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        when(failoverProperties.getThreshold()).thenReturn(100);
        when(failoverProperties.getKeepSize()).thenReturn(10);

        long height = 1L;

        do {
            packageCache.receivePackageHeight(height);
        } while (++height <= 100);
        packageCache.clean();
        assertEquals(packageCache.getMinHeight(), SyncPackageCache.INIT_HEIGHT);
        assertEquals(packageCache.getLatestHeight(), SyncPackageCache.INIT_HEIGHT);
    }

    @Test public void testStateChanged() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        when(failoverProperties.getThreshold()).thenReturn(100);
        when(failoverProperties.getKeepSize()).thenReturn(10);

        long height = 1L;
        do {
            packageCache.receivePackageHeight(height);
        } while (++height <= 100);
        packageCache.stateChanged(null, null);
        assertEquals(packageCache.getMinHeight(), 1L);
        assertEquals(packageCache.getLatestHeight(), 100L);

        packageCache.stateChanged(NodeStateEnum.Starting, NodeStateEnum.Running);
        assertEquals(packageCache.getMinHeight(), 1L);
        assertEquals(packageCache.getLatestHeight(), 100L);

        packageCache.stateChanged(NodeStateEnum.AutoSync, NodeStateEnum.Running);
        assertEquals(packageCache.getMinHeight(), SyncPackageCache.INIT_HEIGHT);
        assertEquals(packageCache.getLatestHeight(), SyncPackageCache.INIT_HEIGHT);
    }

    @Test public void testAfterPropertiesSet() {
        packageCache.afterPropertiesSet();
        verify(nodeState).registerStateListener(packageCache);
    }

}