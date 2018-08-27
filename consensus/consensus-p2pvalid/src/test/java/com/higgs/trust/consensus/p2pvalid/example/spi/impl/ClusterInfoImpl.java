package com.higgs.trust.consensus.p2pvalid.example.spi.impl;


import com.higgs.trust.config.p2p.AbstractClusterInfo;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.config.p2p.ClusterInfoVo;

import java.util.List;

/**
 * @author cwy
 */
public class ClusterInfoImpl extends AbstractClusterInfo{

    private String myNodeName;
    private List<String> clusterNodeNames;
    private String pubKey;
    private String privateKey;
    private Integer faultNodeNum;

    private ClusterInfoImpl() {
    }

    public static ClusterInfoImpl of() {
        return new ClusterInfoImpl();
    }

    public ClusterInfoImpl setMyNodeName(String myNodeName) {
        this.myNodeName = myNodeName;
        return this;
    }

    public ClusterInfoImpl setPubKey(String pubKey) {
        this.pubKey = pubKey;
        return this;
    }

    public ClusterInfoImpl setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public ClusterInfoImpl setClusterNodeNames(List<String> clusterNodeNames) {
        this.clusterNodeNames = clusterNodeNames;
        return this;
    }

    public ClusterInfoImpl setFaultNodeNum(Integer faultNodeNum) {
        this.faultNodeNum = faultNodeNum;
        return this;
    }

    @Override
    public Integer faultNodeNum() {
        return faultNodeNum;
    }

    @Override
    public String nodeName() {
        return myNodeName;
    }

    @Override
    public List<String> clusterNodeNames() {
        return clusterNodeNames;
    }

    @Override
    public String pubKeyForConsensus(String nodeName) {
        return pubKey;
    }

    /**
     * get public key create the given nodeName
     *
     * @param nodeName
     * @return
     */
    @Override public String pubKeyForBiz(String nodeName) {
        return null;
    }

    @Override
    public String priKeyForConsensus() {
        return privateKey;
    }

    /**
     * get the self private key
     *
     * @return
     */
    @Override public String priKeyForBiz() {
        return null;
    }

    @Override public void init(ClusterInfoVo vo) {

    }

    @Override public void refresh() {

    }

    @Override public void refreshConsensus() {

    }
}
