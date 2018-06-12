package com.higgs.trust.slave.core.managment;

import com.higgs.trust.slave.common.config.NodeProperties;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.FailoverExecption;
import com.higgs.trust.slave.core.managment.listener.MasterChangeListener;
import com.higgs.trust.slave.core.managment.listener.StateChangeListener;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/*
 * Copyright (c) 2013-2017, suimi
 */

@RunWith(PowerMockRunner.class) public class NodeStateTest {

    @InjectMocks private NodeState nodeState;

    @Mock private NodeProperties properties;

    private String nodeName = "prefix_nodeName";
    private String prefix = "prefix";

    @BeforeMethod public void beforeMethod() {
        nodeState = new NodeState();
        MockitoAnnotations.initMocks(this);
        when(properties.getNodeName()).thenReturn(nodeName);
        when(properties.getPrefix()).thenReturn(prefix);
        nodeState.afterPropertiesSet();
    }

    @Test public void testRegisterStateListener() {
        StateChangeListener stateChangeListener = mock(StateChangeListener.class);
        nodeState.registerStateListener(stateChangeListener);
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        verify(stateChangeListener, times(1)).stateChanged(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
    }

    @Test public void testRegisterStateListenerNull() {
        nodeState.registerStateListener(null);
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
    }

    @Test public void testRegisterStateListenerDuplicate() {
        StateChangeListener stateChangeListener = mock(StateChangeListener.class);
        nodeState.registerStateListener(stateChangeListener);
        nodeState.registerStateListener(stateChangeListener);
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        verify(stateChangeListener, times(1)).stateChanged(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
    }

    @Test public void testRegisterMasterListener() {
        MasterChangeListener masterChangeListener = mock(MasterChangeListener.class);
        nodeState.registerMasterListener(masterChangeListener);
        String masterName = "masterName";
        nodeState.changeMaster(masterName);
        verify(masterChangeListener, times(1)).masterChanged(masterName);
    }

    @Test public void testChangeMaster() {
        String newMasterName = "newMasterName";
        nodeState.changeMaster(newMasterName);
        assertEquals(nodeState.getMasterName(), newMasterName);
        assertFalse(nodeState.isMaster());

        nodeState.changeMaster(nodeName);
        assertEquals(nodeState.getMasterName(), nodeName);
        assertTrue(nodeState.isMaster());

    }

    @DataProvider public Object[][] starting() {
        return new Object[][] {new Object[] {NodeStateEnum.SelfChecking},};
    }

    @DataProvider public Object[][] startingException() {
        return new Object[][] {new Object[] {NodeStateEnum.Starting},
            //            new Object[]{NodeStateEnum.SelfChecking},
            new Object[] {NodeStateEnum.AutoSync}, new Object[] {NodeStateEnum.ArtificialSync},
            new Object[] {NodeStateEnum.Running}, new Object[] {NodeStateEnum.Offline},};
    }

    @Test(dataProvider = "starting") public void testChangeStateStarting(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, toState);
        assertEquals(nodeState.getState(), toState);
    }

    @Test(dataProvider = "startingException") public void testChangeStateStartingException(NodeStateEnum toState) {
        try {
            nodeState.changeState(NodeStateEnum.Starting, toState);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_STATE_CHANGE_FAILED);
        }
        assertEquals(nodeState.getState(), NodeStateEnum.Starting);
    }

    @DataProvider public Object[][] self() {
        return new Object[][] {new Object[] {NodeStateEnum.AutoSync}, new Object[] {NodeStateEnum.ArtificialSync},
            new Object[] {NodeStateEnum.Running}, new Object[] {NodeStateEnum.Offline},};
    }

    @DataProvider public Object[][] selfException() {
        return new Object[][] {new Object[] {NodeStateEnum.Starting}, new Object[] {NodeStateEnum.SelfChecking},};
    }

    @Test(dataProvider = "self") public void testChangeStateSelf(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, toState);
        assertEquals(nodeState.getState(), toState);
    }

    @Test(dataProvider = "selfException") public void testChangeStateSelfException(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        try {
            nodeState.changeState(NodeStateEnum.SelfChecking, toState);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_STATE_CHANGE_FAILED);
        }
        assertEquals(nodeState.getState(), NodeStateEnum.SelfChecking);
    }

    @DataProvider public Object[][] sync() {
        return new Object[][] {new Object[] {NodeStateEnum.SelfChecking}, new Object[] {NodeStateEnum.Running},
            new Object[] {NodeStateEnum.Offline}};
    }

    @DataProvider public Object[][] syncException() {
        return new Object[][] {new Object[] {NodeStateEnum.Starting}, new Object[] {NodeStateEnum.AutoSync},
            new Object[] {NodeStateEnum.ArtificialSync}};
    }

    @Test(dataProvider = "sync") public void testChangeStateAutoSync(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
        nodeState.changeState(NodeStateEnum.AutoSync, toState);
        assertEquals(nodeState.getState(), toState);
    }

    @Test(dataProvider = "syncException") public void testChangeStateAutoSyncException(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
        try {
            nodeState.changeState(NodeStateEnum.AutoSync, toState);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_STATE_CHANGE_FAILED);
        }
        assertEquals(nodeState.getState(), NodeStateEnum.AutoSync);
    }

    @Test(dataProvider = "sync") public void testChangeStateArtificialSync(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.ArtificialSync);
        nodeState.changeState(NodeStateEnum.ArtificialSync, toState);
        assertEquals(nodeState.getState(), toState);
    }

    @Test(dataProvider = "syncException") public void testChangeStateArtificialSyncException(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.ArtificialSync);
        try {
            nodeState.changeState(NodeStateEnum.ArtificialSync, toState);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_STATE_CHANGE_FAILED);
        }
        assertEquals(nodeState.getState(), NodeStateEnum.ArtificialSync);
    }

    @DataProvider public Object[][] running() {
        return new Object[][] {new Object[] {NodeStateEnum.SelfChecking}, new Object[] {NodeStateEnum.Offline}};
    }

    @DataProvider public Object[][] runningException() {
        return new Object[][] {new Object[] {NodeStateEnum.Starting}, new Object[] {NodeStateEnum.AutoSync},
            new Object[] {NodeStateEnum.ArtificialSync}, new Object[] {NodeStateEnum.Running},};
    }

    @Test(dataProvider = "running") public void testChangeStateRunning(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Running);
        nodeState.changeState(NodeStateEnum.Running, toState);
        assertEquals(nodeState.getState(), toState);
    }

    @Test(dataProvider = "runningException") public void testChangeStateRunningException(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Running);
        try {
            nodeState.changeState(NodeStateEnum.Running, toState);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_STATE_CHANGE_FAILED);
        }
        assertEquals(nodeState.getState(), NodeStateEnum.Running);
    }

    @DataProvider public Object[][] offline() {
        return new Object[][] {new Object[] {NodeStateEnum.SelfChecking},};
    }

    @DataProvider public Object[][] offlineException() {
        return new Object[][] {new Object[] {NodeStateEnum.Starting}, new Object[] {NodeStateEnum.AutoSync},
            new Object[] {NodeStateEnum.ArtificialSync}, new Object[] {NodeStateEnum.Running},
            new Object[] {NodeStateEnum.Offline},};
    }

    @Test(dataProvider = "offline") public void testChangeStateOffline(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Offline);
        nodeState.changeState(NodeStateEnum.Offline, toState);
        assertEquals(nodeState.getState(), toState);
    }

    @Test(dataProvider = "offlineException") public void testChangeStateOfflineException(NodeStateEnum toState) {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Offline);
        try {
            nodeState.changeState(NodeStateEnum.Offline, toState);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_STATE_CHANGE_FAILED);
        }
        assertEquals(nodeState.getState(), NodeStateEnum.Offline);
    }

    @Test public void testIsState() {
        assertTrue(nodeState.isState(NodeStateEnum.Starting));
        assertFalse(nodeState.isState(NodeStateEnum.AutoSync));
        assertTrue(nodeState.isState(NodeStateEnum.Starting, NodeStateEnum.Running));
        assertFalse(nodeState.isState(NodeStateEnum.AutoSync, NodeStateEnum.Running));
    }

    @Test public void testNotMeNodeNameReg() {
        assertEquals(nodeState.notMeNodeNameReg(),
            "(?!" + nodeName.toUpperCase() + ")" + prefix.toUpperCase() + "(\\S)*");

    }
}