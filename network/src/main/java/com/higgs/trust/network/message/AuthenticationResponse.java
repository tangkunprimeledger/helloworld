package com.higgs.trust.network.message;

import com.higgs.trust.network.Peer;

import java.io.Serializable;
import java.util.List;

/**
 * @author duhongming
 * @date 2018/8/31
 */
public class AuthenticationResponse implements Serializable {
    private String message;
    private List<Peer> peers;
    private Peer peer;

    public AuthenticationResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }
}
