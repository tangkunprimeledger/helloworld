package com.higgs.trust.config.filter;

import com.higgs.trust.config.node.command.ViewCommand;
import com.higgs.trust.config.view.ClusterOptTx;
import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.ClusterViewManager;
import com.higgs.trust.config.view.LastPackage;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.filter.CommandFilterChain;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

/*
 * Copyright (c) 2013-2017, suimi
 */
public class ClusterViewFilterTest {

    @Autowired @InjectMocks private ClusterViewFilter filter;

    @Mock private ClusterViewManager viewManager;

    @BeforeClass public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod public void beforeMethod() {
        Mockito.reset(viewManager);
    }

    @Test public void testDoFilterOldView() {
        ClusterView view = new ClusterView(1, 0, 1, 10, new HashMap<>());
        ClusterView view2 = new ClusterView(2, 0, 10, 20, new HashMap<>());
        ClusterOptTx optTx = new ClusterOptTx();
        Mockito.when(viewManager.getCurrentView()).thenReturn(view2);
        LastPackage lastPackage = new LastPackage(1L, 1L);
        Mockito.when(viewManager.getLastPackage()).thenReturn(lastPackage);
        //---not current view
        //view not exist
        Mockito.when(viewManager.getView(Mockito.anyLong())).thenReturn(null);
        ConsensusCommit<? extends AbstractConsensusCommand> commit = buildCommit(1, 11, optTx);
        CommandFilterChain filterChain = Mockito.mock(CommandFilterChain.class);
        filter.doFilter(commit, filterChain);
        Assert.assertTrue(commit.isClosed());
        Mockito.verify(filterChain, Mockito.times(0)).doFilter(commit);
        Mockito.verify(viewManager, Mockito.times(0)).changeView(Mockito.any());

        // is old view package
        Mockito.when(viewManager.getView(Mockito.anyLong())).thenReturn(view);
        commit = buildCommit(1, 10, optTx);
        filterChain = Mockito.mock(CommandFilterChain.class);
        filter.doFilter(commit, filterChain);
        Assert.assertFalse(commit.isClosed());
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(commit);
        Mockito.verify(viewManager, Mockito.times(0)).changeView(Mockito.any());

        //is old view, but not view package
        Mockito.reset(viewManager);
        Mockito.when(viewManager.getLastPackage()).thenReturn(lastPackage);
        Mockito.when(viewManager.getCurrentView()).thenReturn(view2);
        Mockito.when(viewManager.getView(Mockito.anyLong())).thenReturn(view);
        commit = buildCommit(1, 11, optTx);
        filterChain = Mockito.mock(CommandFilterChain.class);
        filter.doFilter(commit, filterChain);
        Assert.assertTrue(commit.isClosed());
        Mockito.verify(filterChain, Mockito.times(0)).doFilter(commit);
        Mockito.verify(viewManager, Mockito.times(0)).changeView(Mockito.any());
    }

    @Test public void testDoFilterCurrentView() {
        ClusterOptTx optTx = new ClusterOptTx();
        ClusterView view = new ClusterView(2, 0, 10, -1, new HashMap<>());
        Mockito.when(viewManager.getCurrentView()).thenReturn(view);
        LastPackage lastPackage = new LastPackage(1L, 1L);
        Mockito.when(viewManager.getLastPackage()).thenReturn(lastPackage);
        //---current view
        //start package
        Mockito.when(viewManager.getView(Mockito.anyLong())).thenReturn(view);
        ConsensusCommit<? extends AbstractConsensusCommand> commit = buildCommit(2, 10, optTx);
        CommandFilterChain filterChain = Mockito.mock(CommandFilterChain.class);
        filter.doFilter(commit, filterChain);
        Mockito.verify(viewManager, Mockito.times(1)).resetEndHeight(10);
        Assert.assertFalse(commit.isClosed());
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(commit);
        Mockito.verify(viewManager, Mockito.times(1)).changeView(Mockito.any());

        //next package
        Mockito.reset(viewManager);
        Mockito.when(viewManager.getLastPackage()).thenReturn(lastPackage);
        view = new ClusterView(2, 0, 10, 12, new HashMap<>());
        Mockito.when(viewManager.getCurrentView()).thenReturn(view);
        Mockito.when(viewManager.getView(Mockito.anyLong())).thenReturn(view);
        commit = buildCommit(2, 13, optTx);
        filterChain = Mockito.mock(CommandFilterChain.class);
        filter.doFilter(commit, filterChain);
        Mockito.verify(viewManager, Mockito.times(1)).resetEndHeight(13);
        Assert.assertFalse(commit.isClosed());
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(commit);
        Mockito.verify(viewManager, Mockito.times(1)).changeView(Mockito.any());

        // old package
        Mockito.reset(viewManager);
        Mockito.when(viewManager.getLastPackage()).thenReturn(lastPackage);
        commit = buildCommit(2, 12, optTx);
        Mockito.when(viewManager.getCurrentView()).thenReturn(view);
        Mockito.when(viewManager.getView(Mockito.anyLong())).thenReturn(view);
        filterChain = Mockito.mock(CommandFilterChain.class);
        filter.doFilter(commit, filterChain);
        Mockito.verify(viewManager, Mockito.times(0)).resetEndHeight(12);
        Assert.assertFalse(commit.isClosed());
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(commit);
        Mockito.verify(viewManager, Mockito.times(0)).changeView(Mockito.any());

        // out of package
        Mockito.reset(viewManager);
        Mockito.when(viewManager.getLastPackage()).thenReturn(lastPackage);
        commit = buildCommit(2, 8, optTx);
        Mockito.when(viewManager.getCurrentView()).thenReturn(view);
        Mockito.when(viewManager.getView(Mockito.anyLong())).thenReturn(view);
        filterChain = Mockito.mock(CommandFilterChain.class);
        filter.doFilter(commit, filterChain);
        Mockito.verify(viewManager, Mockito.times(0)).resetEndHeight(8);
        Assert.assertTrue(commit.isClosed());
        Mockito.verify(filterChain, Mockito.times(0)).doFilter(commit);
        Mockito.verify(viewManager, Mockito.times(0)).changeView(Mockito.any());
    }

    @Test public void testPackTimeReject(){
        Mockito.when(viewManager.getLastPackage()).thenReturn(new LastPackage(10L,1L));
        ConsensusCommit<? extends AbstractConsensusCommand> commit = buildCommit(1, 11, 0,null);
        CommandFilterChain filterChain = Mockito.mock(CommandFilterChain.class);
        filter.doFilter(commit, filterChain);
        Assert.assertTrue(commit.isClosed());
    }

    @Test public void testPackTime(){
        LastPackage lastPackage = new LastPackage(9L, 1L);
        Mockito.when(viewManager.getLastPackage()).thenReturn(lastPackage);
        ClusterView view = new ClusterView(2, 0, 10, -1, new HashMap<>());
        Mockito.when(viewManager.getCurrentView()).thenReturn(view);
        Mockito.when(viewManager.getView(Mockito.anyLong())).thenReturn(view);
        ConsensusCommit<? extends AbstractConsensusCommand> commit = buildCommit(2, 10, 2,null);
        CommandFilterChain filterChain = Mockito.mock(CommandFilterChain.class);
        filter.doFilter(commit, filterChain);
        Mockito.verify(viewManager, Mockito.times(1)).resetEndHeight(10);
        Assert.assertFalse(commit.isClosed());
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(commit);
        Mockito.verify(viewManager, Mockito.times(1)).resetLastPackage(Mockito.any());
    }


    private ConsensusCommit buildCommit(long view, long height, ClusterOptTx optTx) {
        return buildCommit(view, height, System.currentTimeMillis(), optTx);
    }

    private ConsensusCommit buildCommit(long view, long height, long packTime, ClusterOptTx optTx) {
        return new ConsensusCommit() {
            private boolean closed = false;

            @Override public Object operation() {
                return new PackageMockCommand(view, height, packTime, optTx);
            }

            @Override public void close() {
                closed = true;
            }

            @Override public boolean isClosed() {
                return closed;
            }
        };
    }

    private class PackageMockCommand extends AbstractConsensusCommand implements ViewCommand {

        private long view;

        private long height;

        private long packTime;

        private ClusterOptTx clusterOptTx;

        public PackageMockCommand(long view, long height, long packTime, ClusterOptTx optTx) {
            super(optTx);
            this.view = view;
            this.height = height;
            this.clusterOptTx = optTx;
            this.packTime = packTime;
        }

        @Override public long getView() {
            return view;
        }

        @Override public long getPackageHeight() {
            return height;
        }

        @Override public long getPackageTime() {
            return packTime;
        }

        @Override public ClusterOptTx getClusterOptTx() {
            return clusterOptTx;
        }
    }
}