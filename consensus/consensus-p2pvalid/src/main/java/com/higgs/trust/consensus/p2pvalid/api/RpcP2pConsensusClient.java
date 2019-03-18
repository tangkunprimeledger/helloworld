package com.higgs.trust.consensus.p2pvalid.api;

import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import com.higgs.trust.network.Address;
import com.higgs.trust.network.NetworkManage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author duhongming
 * @date 2018/9/18
 */
@ConditionalOnProperty(name = "network.rpc", havingValue = "netty", matchIfMissing = true)
@Component
@Slf4j
public class RpcP2pConsensusClient implements P2pConsensusClient {

    private static final String ACTION_TYPE_RECEIVE_COMMAND = "consensus/p2p/receive_command";
    private static final String ACTION_TYPE_RECEIVE_COMMAND_SYNC = "consensus/p2p/receive_command_sync";

    @Autowired
    private IClusterViewManager viewManager;

    @Autowired
    private NetworkManage networkManage;

    public RpcP2pConsensusClient() {
        log.info("Use RpcP2pConsensusClient");
    }

    @Override
    public ValidResponseWrap<ResponseCommand> send(String nodeName, ValidCommandWrap validCommandWrap) {
        Address to = networkManage.getAddress(nodeName);
        try {
            return networkManage.<ValidResponseWrap<ResponseCommand>>send(to, ACTION_TYPE_RECEIVE_COMMAND, validCommandWrap).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ValidResponseWrap<ResponseCommand> syncSend(String nodeName, ValidCommandWrap validCommandWrap) {
        Address to = networkManage.getAddress(nodeName);
        if (to == null) {
            throw new RuntimeException(String.format("Node %s unavailable ", nodeName));
        }
        try {
            return networkManage.<ValidResponseWrap<ResponseCommand>>send(to, ACTION_TYPE_RECEIVE_COMMAND_SYNC, validCommandWrap).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ValidResponseWrap<ResponseCommand> syncSendFeign(String nodeNameReg, ValidCommandWrap validCommandWrap) {
        Address to = getRandomPeerAddress();
        try {
            return networkManage.<ValidResponseWrap<ResponseCommand>>send(to, ACTION_TYPE_RECEIVE_COMMAND_SYNC, validCommandWrap).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Address getRandomPeerAddress() {
        List<String> names = viewManager.getCurrentView().getNodeNames();
        String nodeName = networkManage.httpClient().getRandomPeer(names).getNodeName();
        return networkManage.getAddress(nodeName);
    }
}
