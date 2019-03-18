package com.higgs.trust.network.message;

import com.higgs.trust.network.Peer;

import java.io.Serializable;
import java.util.Set;

/**
 * @author duhongming
 * @date 2018/8/30
 */
public class DiscoveryPeersResponse implements Serializable {
    private Set<Peer> peers;

    public DiscoveryPeersResponse(Set<Peer> peers) {
        this.peers = peers;
    }

    public Set<Peer> getPeers() {
        return peers;
    }

    public void setPeers(Set<Peer> peers) {
        this.peers = peers;
    }
}
