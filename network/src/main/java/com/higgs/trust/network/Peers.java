package com.higgs.trust.network;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author duhongming
 * @date 2018/9/6
 */
public final class Peers {

    private final Logger log = LoggerFactory.getLogger(getClass());

    final Map<Address, Peer> map = Maps.newConcurrentMap();
    Address localAddress;
    Peer localPeer;

    public void init(final Address localAddress, final List<Peer> seeds, final NetworkConfig config) {
        this.localAddress = localAddress;
        localPeer = config.localPeer();
        map.put(localAddress, localPeer);
        seeds.forEach(peer -> {
            map.putIfAbsent(peer.getAddress(), peer);
        });
    }

    public Peer getByAddress(Address address) {
        return map.get(address);
    }

    public Set<Peer> getPeers() {
        return Sets.newConcurrentHashSet(map.values());
    }

    public Peer getPeer(String nodeName) {
        for (Peer peer : map.values()) {
            if (nodeName.equals(peer.getNodeName()) && !peer.isSlave()) {
                return peer;
            }
        }
        return null;
    }

    public Peer getBackupPeer(String nodeName) {
        for (Peer peer : map.values()) {
            if (nodeName.equals(peer.getNodeName()) && peer.isSlave()) {
                return peer;
            }
        }
        return null;
    }

    public Address getAddress(String nodeName) {
        Peer peer = getPeer(nodeName);
        return peer == null ? null : peer.getAddress();
    }

    public Peer get(Address address) {
        return map.get(address);
    }

    public Peer put(Peer peer) {
        return map.put(peer.getAddress(), peer);
    }

    public Peer putIfAbsent(Peer peer) {
        return map.putIfAbsent(peer.getAddress(), peer);
    }

    public void updatePeerConnected(Address address, boolean connected) {
        Peer peer = map.get(address);
        if (peer != null) {
            peer.setConnected(connected);
        } else {
            log.warn("{} not fond", address);
        }
    }
}
