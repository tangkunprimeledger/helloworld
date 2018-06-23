package com.higgs.trust.management.failover.service;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.management.failover.config.FailoverProperties;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

    long clusterHeight = 1L;

    @BeforeClass public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod public void beforMethod() {

        packageCache.reset(clusterHeight);
    }

    @Test public void testReceiveNot() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(false);
        packageCache.receivePackHeight(2L);
        assertEquals(packageCache.getMinHeight(), clusterHeight);
    }

    @Test public void testReceive() {
        long clusterHeight = 1L;
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        packageCache.reset(clusterHeight);
        assertEquals(packageCache.getMinHeight(), clusterHeight);
        assertEquals(packageCache.getLatestHeight(), clusterHeight);

        when(failoverProperties.getThreshold()).thenReturn(100);
        when(failoverProperties.getKeepSize()).thenReturn(10);
        long height = 1L;
        packageCache.receivePackHeight(height);
        assertEquals(packageCache.getMinHeight(), height);
        assertEquals(packageCache.getLatestHeight(), height);

        height = 2L;
        packageCache.receivePackHeight(height);
        assertEquals(packageCache.getMinHeight(), 1L);
        assertEquals(packageCache.getLatestHeight(), height);

        height = 1L;
        packageCache.receivePackHeight(height);
        assertEquals(packageCache.getMinHeight(), 1L);
        assertEquals(packageCache.getLatestHeight(), 2L);

        height = 3L;
        packageCache.receivePackHeight(height);
        assertEquals(packageCache.getMinHeight(), 1L);
        assertEquals(packageCache.getLatestHeight(), height);

        height = 5L;
        packageCache.receivePackHeight(height);
        assertEquals(packageCache.getMinHeight(), height);
        assertEquals(packageCache.getLatestHeight(), height);
    }

    @Test public void testReceiveOutThreshold() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        when(failoverProperties.getThreshold()).thenReturn(100);
        when(failoverProperties.getKeepSize()).thenReturn(10);

        long height = 1L;

        do {
            packageCache.receivePackHeight(height);
            assertEquals(packageCache.getMinHeight(), 1L);
            assertEquals(packageCache.getLatestHeight(), height);
        } while (++height <= 100);

        packageCache.receivePackHeight(101L);
        assertEquals(packageCache.getMinHeight(), 91L);
        assertEquals(packageCache.getLatestHeight(), 101L);
    }

    @Test public void testReceiveOutThreshold2() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        when(failoverProperties.getThreshold()).thenReturn(10);
        when(failoverProperties.getKeepSize()).thenReturn(11);

        long height = 1L;

        do {
            packageCache.receivePackHeight(height);
            assertEquals(packageCache.getMinHeight(), 1L);
            assertEquals(packageCache.getLatestHeight(), height);
        } while (++height <= 10);

        packageCache.receivePackHeight(11L);
        assertEquals(packageCache.getMinHeight(), 11L);
        assertEquals(packageCache.getLatestHeight(), 11L);

        do {
            packageCache.receivePackHeight(height);
            assertEquals(packageCache.getMinHeight(), 11L);
            assertEquals(packageCache.getLatestHeight(), height);
        } while (++height <= 20);

        packageCache.receivePackHeight(21L);
        assertEquals(packageCache.getMinHeight(), 21L);
        assertEquals(packageCache.getLatestHeight(), 21L);
    }

    @Test public void testReceiveOutThreshold3() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        when(failoverProperties.getThreshold()).thenReturn(10);
        when(failoverProperties.getKeepSize()).thenReturn(10);

        long height = 1L;

        do {
            packageCache.receivePackHeight(height);
            assertEquals(packageCache.getMinHeight(), 1L);
            assertEquals(packageCache.getLatestHeight(), height);
        } while (++height <= 10);

        packageCache.receivePackHeight(11L);
        assertEquals(packageCache.getMinHeight(), 11L);
        assertEquals(packageCache.getLatestHeight(), 11L);

        do {
            packageCache.receivePackHeight(height);
            assertEquals(packageCache.getMinHeight(), 11L);
            assertEquals(packageCache.getLatestHeight(), height);
        } while (++height <= 20);

        packageCache.receivePackHeight(21L);
        assertEquals(packageCache.getMinHeight(), 21L);
        assertEquals(packageCache.getLatestHeight(), 21L);
    }

    @Test public void testReset() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        when(failoverProperties.getThreshold()).thenReturn(100);
        when(failoverProperties.getKeepSize()).thenReturn(10);

        long height = 1L;

        do {
            packageCache.receivePackHeight(height);
        } while (++height <= 100);
        long clusterHeight = 10;
        packageCache.reset(clusterHeight);
        assertEquals(packageCache.getMinHeight(), clusterHeight);
        assertEquals(packageCache.getLatestHeight(), clusterHeight);
    }

}