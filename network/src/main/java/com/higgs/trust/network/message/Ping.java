package com.higgs.trust.network.message;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/8/21
 */
public class Ping {

    private List<String> peers;

    public List<String> getPeers() {
        return peers;
    }

    public void setPeers(List<String> peers) {
        this.peers = peers;
    }
}
