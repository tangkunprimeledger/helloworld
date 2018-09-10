package com.higgs.trust.config.view;

import com.higgs.trust.common.crypto.rsa.RsaCrypto;
import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.exception.ConfigException;
import com.higgs.trust.config.node.command.ViewCommand;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.anyString;

/*
 * Copyright (c) 2013-2017, suimi
 */
@PrepareForTest({CryptoUtil.class}) public class ClusterViewManagerTest extends PowerMockTestCase {

    private ClusterViewManager viewManager;

    @BeforeMethod public void before() {
        viewManager = new ClusterViewManager();
    }

    @Test public void testResetViews() {
        Assert.assertEquals(0, viewManager.getViews().size());
        Assert.assertNull(viewManager.getCurrentView());

        viewManager.resetViews(null);
        Assert.assertEquals(0, viewManager.getViews().size());
        Assert.assertNull(viewManager.getCurrentView());

        viewManager.resetViews(Collections.emptyList());
        Assert.assertEquals(0, viewManager.getViews().size());
        Assert.assertNull(viewManager.getCurrentView());

        ClusterView view = new ClusterView(1, 1, new HashMap<>());
        viewManager.resetViews(Collections.singletonList(view));
        Assert.assertEquals(1, viewManager.getViews().size());
        Assert.assertEquals(viewManager.getCurrentView(), view);
        Assert.assertNotSame(viewManager.getCurrentView(), view);

        ClusterView view2 = new ClusterView(2, 31, new HashMap<>());
        ArrayList<ClusterView> views = new ArrayList<>();
        views.add(view);
        views.add(view2);
        viewManager.resetViews(views);
        Assert.assertEquals(2, viewManager.getViews().size());
        Assert.assertEquals(viewManager.getCurrentView(), view2);
        Assert.assertNotSame(viewManager.getCurrentView(), view2);

    }

    @Test public void testGetViews() {
        ClusterView view = new ClusterView(1, 1, new HashMap<>());
        ClusterView view2 = new ClusterView(2, 31, new HashMap<>());
        ArrayList<ClusterView> views = new ArrayList<>();
        views.add(view);
        views.add(view2);
        viewManager.resetViews(views);
        List<ClusterView> gotViews = viewManager.getViews();
        Assert.assertEquals(2, gotViews.size());
        Assert.assertEquals(views, gotViews);
        Assert.assertNotSame(views, gotViews);

        ClusterView view3 = new ClusterView(3, 43, new HashMap<>());
        views.add(view3);
        gotViews = viewManager.getViews();
        Assert.assertEquals(2, gotViews.size());
        Assert.assertNotEquals(views, gotViews);
        Assert.assertNotSame(views, gotViews);
    }

    @Test public void testGetViewWithHeight() {
        ClusterView view = new ClusterView(1, 0, 1, 10, new HashMap<>());
        ClusterView view2 = new ClusterView(2, 0, 11, 20, new HashMap<>());
        ClusterView view3 = new ClusterView(3, 21, new HashMap<>());
        ArrayList<ClusterView> views = new ArrayList<>();
        views.add(view);
        views.add(view2);
        views.add(view3);
        viewManager.resetViews(views);
        Assert.assertEquals(viewManager.getViewWithHeight(1), view);
        Assert.assertNotSame(viewManager.getViewWithHeight(1), view);
        Assert.assertEquals(viewManager.getViewWithHeight(5), view);
        Assert.assertNotSame(viewManager.getViewWithHeight(5), view);
        Assert.assertEquals(viewManager.getViewWithHeight(10), view);
        Assert.assertNotSame(viewManager.getViewWithHeight(10), view);
        Assert.assertEquals(viewManager.getViewWithHeight(11), view2);
        Assert.assertNotSame(viewManager.getViewWithHeight(11), view2);
        Assert.assertEquals(viewManager.getViewWithHeight(20), view2);
        Assert.assertNotSame(viewManager.getViewWithHeight(20), view2);
        Assert.assertEquals(viewManager.getViewWithHeight(21), view3);
        Assert.assertNotSame(viewManager.getViewWithHeight(21), view3);
        Assert.assertEquals(viewManager.getViewWithHeight(23), view3);
        Assert.assertNotSame(viewManager.getViewWithHeight(23), view3);
    }

    @Test public void testGetView() {
        ClusterView view = new ClusterView(1, 0, 1, 10, new HashMap<>());
        ClusterView view2 = new ClusterView(2, 0, 11, 20, new HashMap<>());
        ClusterView view3 = new ClusterView(3, 21, new HashMap<>());
        ArrayList<ClusterView> views = new ArrayList<>();
        views.add(view);
        views.add(view2);
        views.add(view3);
        viewManager.resetViews(views);
        Assert.assertEquals(viewManager.getView(-1), view3);
        Assert.assertNotSame(viewManager.getView(-1), view3);
        Assert.assertEquals(viewManager.getView(-2), view3);
        Assert.assertNotSame(viewManager.getView(-2), view3);
        Assert.assertNull(viewManager.getView(0));
        Assert.assertEquals(viewManager.getView(1), view);
        Assert.assertNotSame(viewManager.getView(1), view);
        Assert.assertEquals(viewManager.getView(2), view2);
        Assert.assertNotSame(viewManager.getView(2), view2);
        Assert.assertEquals(viewManager.getView(3), view3);
        Assert.assertNotSame(viewManager.getView(3), view3);
    }

    @PrepareForTest({CryptoUtil.class}) @Test public void testChangeViewNull() {
        RsaCrypto mock = PowerMockito.mock(RsaCrypto.class);
        PowerMockito.mockStatic(CryptoUtil.class);
        PowerMockito.when(CryptoUtil.getProtocolCrypto()).thenReturn(mock);
        BDDMockito.given(mock.verify(anyString(), anyString(), anyString())).willReturn(true);

        ClusterView view = new ClusterView(1, 0, 1, 10, new HashMap<>());
        ArrayList<ClusterView> views = new ArrayList<>();
        views.add(view);
        viewManager.resetViews(views);
        ViewCommand viewCommand = new ViewCommand() {
            @Override public long getView() {
                return 0;
            }

            @Override public long getPackageHeight() {
                return 0;
            }

            @Override public ClusterOptTx getClusterOptTx() {
                return null;
            }
        };
        viewManager.changeView(viewCommand);
        Assert.assertEquals(viewManager.getCurrentView(), view);
    }

    @PrepareForTest({CryptoUtil.class}) @Test public void testChangeViewSignFailed() {
        RsaCrypto mock = PowerMockito.mock(RsaCrypto.class);
        PowerMockito.mockStatic(CryptoUtil.class);
        PowerMockito.when(CryptoUtil.getProtocolCrypto()).thenReturn(mock);

        HashMap<String, String> nodes = new HashMap<>();
        nodes.put("A", "Apk");
        nodes.put("B", "Bpk");
        ClusterView view = new ClusterView(1, 0, 1, 10, nodes);
        ArrayList<ClusterView> views = new ArrayList<>();
        views.add(view);
        viewManager.resetViews(views);

        Mockito.when(mock.verify(anyString(), anyString(), anyString())).thenReturn(false);
        ViewCommand viewCommand = buildViewCommand(0, 0, null);
        viewManager.changeView(viewCommand);
        Assert.assertEquals(viewManager.getCurrentView(), view);

        List<ClusterOptTx.SignatureInfo> signList = new ArrayList<>();
        signList.add(new ClusterOptTx.SignatureInfo("A", "Asign"));
        viewCommand = buildViewCommand(0, 0, signList);
        viewManager.changeView(viewCommand);
        Assert.assertEquals(viewManager.getCurrentView(), view);

        Mockito.when(mock.verify(anyString(), anyString(), anyString())).thenReturn(true);
        viewManager.changeView(viewCommand);
        Assert.assertEquals(viewManager.getCurrentView(), view);

        Mockito.when(mock.verify(anyString(), anyString(), anyString())).thenReturn(true).thenReturn(false);
        viewManager.changeView(viewCommand);
        Assert.assertEquals(viewManager.getCurrentView(), view);

        Mockito.when(mock.verify(anyString(), anyString(), anyString())).thenReturn(true);
        signList = new ArrayList<>();
        signList.add(new ClusterOptTx.SignatureInfo("A", "Asign"));
        signList.add(new ClusterOptTx.SignatureInfo("C", "Bsign"));
        viewCommand = buildViewCommand(0, 0, signList);
        viewManager.changeView(viewCommand);
        Assert.assertEquals(viewManager.getCurrentView(), view);

        signList = new ArrayList<>();
        signList.add(new ClusterOptTx.SignatureInfo("A", "Asign"));
        signList.add(new ClusterOptTx.SignatureInfo("B", "Bsign"));
        viewCommand = buildViewCommand(0, 0, signList);
        viewManager.changeView(viewCommand);
        Assert.assertEquals(viewManager.getCurrentView(), view);
    }

    @PrepareForTest({CryptoUtil.class}) @Test public void testChangeViewUnsupportOpt() {
        RsaCrypto mock = PowerMockito.mock(RsaCrypto.class);
        PowerMockito.mockStatic(CryptoUtil.class);
        PowerMockito.when(CryptoUtil.getProtocolCrypto()).thenReturn(mock);
        BDDMockito.given(mock.verify(anyString(), anyString(), anyString())).willReturn(true);

        HashMap<String, String> nodes = new HashMap<>();
        nodes.put("A", "Apk");
        nodes.put("B", "Bpk");
        ClusterView view = new ClusterView(1, 0, 1, 10, nodes);
        ArrayList<ClusterView> views = new ArrayList<>();
        views.add(view);
        viewManager.resetViews(views);

        List signList = new ArrayList<>();
        signList.add(new ClusterOptTx.SignatureInfo("A", "Asign"));
        signList.add(new ClusterOptTx.SignatureInfo("B", "Bsign"));
        ViewCommand viewCommand = buildViewCommand(1, 0, signList);
        Assert.expectThrows(ConfigException.class, () -> viewManager.changeView(viewCommand));
    }

    @PrepareForTest({CryptoUtil.class}) @Test public void testChangeViewJoin() {
        RsaCrypto mock = PowerMockito.mock(RsaCrypto.class);
        PowerMockito.mockStatic(CryptoUtil.class);
        PowerMockito.when(CryptoUtil.getProtocolCrypto()).thenReturn(mock);
        BDDMockito.given(mock.verify(anyString(), anyString(), anyString())).willReturn(true);

        HashMap<String, String> nodes = new HashMap<>();
        nodes.put("A", "Apk");
        nodes.put("B", "Bpk");
        ClusterView view = new ClusterView(1, 0, 1, 10, nodes);
        ArrayList<ClusterView> views = new ArrayList<>();
        views.add(view);
        viewManager.resetViews(views);

        List signList = new ArrayList<>();
        signList.add(new ClusterOptTx.SignatureInfo("A", "Asign"));
        signList.add(new ClusterOptTx.SignatureInfo("B", "Bsign"));
        ViewCommand viewCommand = new ViewCommand() {
            @Override public long getView() {
                return 1;
            }

            @Override public long getPackageHeight() {
                return 11;
            }

            @Override public ClusterOptTx getClusterOptTx() {
                ClusterOptTx clusterOptTx = new ClusterOptTx();
                clusterOptTx.setNodeName("C");
                clusterOptTx.setOperation(ClusterOptTx.Operation.JOIN);
                clusterOptTx.setSignatureList(signList);
                clusterOptTx.setPubKey("Cpk");
                return clusterOptTx;
            }
        };
        viewManager.changeView(viewCommand);
        ClusterView currentView = viewManager.getCurrentView();
        Assert.assertEquals(viewManager.getViews().size(), 2);
        Assert.assertEquals(currentView.getId(), 2);
        Assert.assertEquals(currentView.getStartHeight(), 12);
        Assert.assertEquals(currentView.getEndHeight(), -1);
        Assert.assertTrue(currentView.getNodes().containsKey("C"));
        Assert.assertEquals(currentView.getNodes().get("C"), "Cpk");
    }

    @PrepareForTest({CryptoUtil.class}) @Test public void testChangeViewLeave() {
        RsaCrypto mock = PowerMockito.mock(RsaCrypto.class);
        PowerMockito.mockStatic(CryptoUtil.class);
        PowerMockito.when(CryptoUtil.getProtocolCrypto()).thenReturn(mock);
        BDDMockito.given(mock.verify(anyString(), anyString(), anyString())).willReturn(true);

        HashMap<String, String> nodes = new HashMap<>();
        nodes.put("A", "Apk");
        nodes.put("B", "Bpk");
        nodes.put("C", "Cpk");
        ClusterView view = new ClusterView(1, 0, 1, 10, nodes);
        ArrayList<ClusterView> views = new ArrayList<>();
        views.add(view);
        viewManager.resetViews(views);

        List signList = new ArrayList<>();
        signList.add(new ClusterOptTx.SignatureInfo("A", "Asign"));
        signList.add(new ClusterOptTx.SignatureInfo("B", "Bsign"));
        signList.add(new ClusterOptTx.SignatureInfo("C", "Csign"));
        ViewCommand viewCommand = new ViewCommand() {
            @Override public long getView() {
                return 1;
            }

            @Override public long getPackageHeight() {
                return 11;
            }

            @Override public ClusterOptTx getClusterOptTx() {
                ClusterOptTx clusterOptTx = new ClusterOptTx();
                clusterOptTx.setNodeName("C");
                clusterOptTx.setOperation(ClusterOptTx.Operation.LEAVE);
                clusterOptTx.setSignatureList(signList);
                clusterOptTx.setPubKey("Cpk");
                return clusterOptTx;
            }
        };
        viewManager.changeView(viewCommand);
        ClusterView currentView = viewManager.getCurrentView();
        Assert.assertEquals(viewManager.getViews().size(), 2);
        Assert.assertEquals(currentView.getId(), 2);
        Assert.assertEquals(currentView.getStartHeight(), 12);
        Assert.assertEquals(currentView.getEndHeight(), -1);
        Assert.assertFalse(currentView.getNodes().containsKey("C"));

        viewManager.changeView(viewCommand);
        Assert.assertEquals(viewManager.getCurrentView(), currentView);
    }

    private ViewCommand buildViewCommand(long view, long height, List<ClusterOptTx.SignatureInfo> signList) {
        return new ViewCommand() {
            @Override public long getView() {
                return view;
            }

            @Override public long getPackageHeight() {
                return height;
            }

            @Override public ClusterOptTx getClusterOptTx() {
                ClusterOptTx clusterOptTx = new ClusterOptTx();
                clusterOptTx.setSignatureList(signList);
                return clusterOptTx;
            }
        };
    }

    @Test public void testResetEndHeight() {
        ClusterView view = new ClusterView(1, 0, 1, 10, new HashMap<>());
        ClusterView view2 = new ClusterView(2, 0, 11, 20, new HashMap<>());
        ClusterView view3 = new ClusterView(3, 21, new HashMap<>());
        ArrayList<ClusterView> views = new ArrayList<>();
        views.add(view);
        views.add(view2);
        views.add(view3);
        viewManager.resetViews(views);
        Assert.expectThrows(ConfigException.class, () -> viewManager.resetEndHeight(20));
        Assert.expectThrows(ConfigException.class, () -> viewManager.resetEndHeight(22));
        viewManager.resetEndHeight(21);
        ClusterView currentView = viewManager.getCurrentView();
        Assert.assertEquals(currentView.getEndHeight(), 21);

        Assert.expectThrows(ConfigException.class, () -> viewManager.resetEndHeight(21));
        Assert.expectThrows(ConfigException.class, () -> viewManager.resetEndHeight(23));

        viewManager.resetEndHeight(22);
        currentView = viewManager.getCurrentView();
        Assert.assertEquals(currentView.getEndHeight(), 22);

        Assert.expectThrows(ConfigException.class, () -> viewManager.resetEndHeight(22));
        Assert.expectThrows(ConfigException.class, () -> viewManager.resetEndHeight(24));
    }
}