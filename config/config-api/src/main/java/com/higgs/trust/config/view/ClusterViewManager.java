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
        currentView = views.size() > 0 ? views.get(views.size() - 1) : null;
    }

    @Override public List<ClusterView> getViews() {
        return (ArrayList<ClusterView>)views.clone();
    }

    @Override public ClusterView getCurrentView() {
        return currentView != null ? currentView.clone() : null;
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
        log.warn("the view for height:{} is not exist, will use current view", height);
        return currentView.clone();
    }

    @Override public ClusterView getView(long viewId) {
        if (viewId <= IClusterViewManager.CURRENT_VIEW_ID) {
            return getCurrentView();
        }
        for (ClusterView view : views) {
            if (viewId == view.getId()) {
                return view.clone();
            }
        }
        return null;
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
            long height = command.getPackageHeight();
            ClusterView newView = new ClusterView(currentView.getId() + 1, height + 1, newNodes);
            views.add(newView);
            currentView = newView;
        }
    }

    @Override public void resetEndHeight(long packageHeight) {
        boolean verify =
            currentView.getEndHeight() == ClusterView.INIT_END_HEIGHT ? packageHeight == currentView.getStartHeight() :
                packageHeight == currentView.getEndHeight() + 1;
        if (!verify) {
            throw new ConfigException(ConfigError.CONFIG_VIEW_PACKAGE_HEIGHT_INCORRECT);
        }
        log.debug("reset currentView end height:{}", packageHeight);
        currentView.setEndHeight(packageHeight);
    }
}
