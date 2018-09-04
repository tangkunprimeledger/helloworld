/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.view;

import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.exception.ConfigError;
import com.higgs.trust.config.exception.ConfigException;
import com.higgs.trust.config.node.command.ViewCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author suimi
 * @date 2018/8/31
 */
@Slf4j @Component public class ClusterViewManager implements IClusterViewManager {

    private ClusterView currentView;

    private ArrayList<ClusterView> views = new ArrayList<>();

    @Override public synchronized void resetViews(List<ClusterView> views) {
        this.views.clear();
        this.views.addAll(views);
        currentView = this.views.get(views.size() - 1);
    }

    @Override public List<ClusterView> getViews() {
        return (ArrayList<ClusterView>)views.clone();
    }

    @Override public ClusterView getCurrentView() {
        return currentView.clone();
    }

    @Override public ClusterView getViewWithHeight(long height) {
        for (ClusterView view : views) {
            if (view.getEndHeight() == ClusterView.INIT_END_HEIGHT) {
                if (height == view.getStartHeight()) {
                    return view.clone();
                }
            } else if (height >= view.getStartHeight() && height <= view.getEndHeight()) {
                return view.clone();
            }
        }
        return null;
    }

    @Override public ClusterView getView(long viewId) {
        for (ClusterView view : views) {
            if (viewId == view.getId()) {
                return view.clone();
            }
        }
        return null;
    }

    @Override public void initViews(ClusterView clusterView) {
        views.clear();
        views.add(clusterView);
        currentView = clusterView;
    }

    @Override public void changeView(ViewCommand command) {
        ClusterOptTx optTx = command.getClusterOptTx();
        if (optTx == null) {
            return;
        }
        //verify node public is right
        boolean verify =
            CryptoUtil.getProtocolCrypto().verify(optTx.getSelfSignValue(), optTx.getSelfSign(), optTx.getPubKey());
        if (!verify) {
            log.warn("ClusterOptTx self sign verify failed");
            return;
        }
        //verify consensus node sign
        String signValue = optTx.getSignatureValue();
        Set<String> verifyedNode = new HashSet<>();
        for (ClusterOptTx.SignatureInfo signInfo : optTx.getSignatureList()) {
            String pubKey = currentView.getPubKey(signInfo.getSigner());
            if (StringUtils.isBlank(pubKey)) {
                log.warn("the public key of node:{} not exist at current view", signInfo.getSigner());
                return;
            }
            boolean signVerify = CryptoUtil.getProtocolCrypto().verify(signValue, signInfo.getSign(), pubKey);
            if (!signVerify) {
                log.warn("the sign of node:{} verify failed", signInfo.getSigner());
                return;
            } else {
                verifyedNode.add(signInfo.getSigner());
            }
        }

        //check have all consensus node sign
        if (verifyedNode.size() != currentView.getNodeNames().size()) {
            log.warn("there is {} node signed, need all consensus node sign", verifyedNode.size());
            return;
        }

        //if the view is current view, then change the view, else maybe the view already changed, it's the replica log
        if (command.getView() == currentView.getId()) {
            Map<String, String> newNodes = new HashMap<>(currentView.getNodes());
            if (optTx.getOperation() == ClusterOptTx.Operation.JOIN) {
                newNodes.put(optTx.getNodeName(), optTx.getPubKey());
            } else if (optTx.getOperation() == ClusterOptTx.Operation.LEAVE) {
                newNodes.remove(optTx.getNodeName());
            } else {
                throw new ConfigException(ConfigError.CONFIG_VIEW_UNSUPPORTED_OPERATION);
            }
            int faultNum = (newNodes.size() - 1) / 3;
            long[] heights = command.getPackageHeight();
            ClusterView newView = new ClusterView(currentView.getId() + 1, faultNum, heights[heights.length - 1] + 1,
                ClusterView.INIT_END_HEIGHT, newNodes);
            views.add(newView);
            currentView = newView;
        }
    }

    @Override public void resetEndHeight(long[] packageHeights) {
        int len = packageHeights.length;
        boolean verify = currentView.getEndHeight() == ClusterView.INIT_END_HEIGHT ?
            packageHeights[0] == currentView.getStartHeight() : packageHeights[0] == currentView.getEndHeight() + 1
            && packageHeights[len - 1] <= currentView.getEndHeight() + len;
        if (!verify) {
            throw new ConfigException(ConfigError.CONFIG_VIEW_PACKAGE_HEIGHT_INCORRECT);
        }
        if ((packageHeights[0] == currentView.getStartHeight()
            && currentView.getEndHeight() == ClusterView.INIT_END_HEIGHT) || (
            packageHeights[0] == currentView.getEndHeight() + 1
                && packageHeights[len - 1] == currentView.getEndHeight() + len)) {
            log.debug("reset currentView end height:{}", packageHeights[len - 1]);
            currentView.setEndHeight(packageHeights[len - 1]);
        } else {
            log.warn("set incorrect end height:{}, currentView:{}", packageHeights[len - 1], currentView);
        }
    }
}
