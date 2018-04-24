package com.higgs.trust.consensus.p2pvalid.example.spi.impl;

import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import java.util.List;

/**
 * @author cwy
 */
public class ClusterInfoImpl implements ClusterInfo {

    private String myNodeName;
    private List<String> clusterNodeNames;
    private String pubKey;
    private String privateKey;
    private Integer faultNodeNum;

    private ClusterInfoImpl(){}

    public static ClusterInfoImpl of(){
        return new ClusterInfoImpl();
    }

    public ClusterInfoImpl setMyNodeName(String myNodeName){
        this.myNodeName = myNodeName;
        return this;
    }

    public ClusterInfoImpl setPubKey(String pubKey){
        this.pubKey = pubKey;
        return this;
    }

    public ClusterInfoImpl setPrivateKey(String privateKey){
        this.privateKey = privateKey;
        return this;
    }

    public ClusterInfoImpl setClusterNodeNames(List<String> clusterNodeNames){
        this.clusterNodeNames = clusterNodeNames;
        return this;
    }

    public ClusterInfoImpl setFaultNodeNum(Integer faultNodeNum){
        this.faultNodeNum = faultNodeNum;
        return this;
    }

    @Override
    public Integer faultNodeNum() {
        return faultNodeNum;
    }

    @Override
    public String myNodeName() {
        return myNodeName;
    }

    @Override
    public List<String> clusterNodeNames() {
        return clusterNodeNames;
    }

    @Override
    public String pubKey(String nodeName) {
        return pubKey;
    }

    @Override
    public String privateKey() {
        return privateKey;
    }
}
