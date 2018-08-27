package com.higgs.trust.config.p2p;

import java.util.List;

/**
 * @author cwy
 */
public interface ClusterInfo extends Refreshable {

    /**
     * get faultNode num
     *
     * @return
     */
    Integer faultNodeNum();

    /**
     * get self node name
     *
     * @return
     */
    String nodeName();

    /**
     * get cluster nodeNames
     *
     * @return
     */
    List<String> clusterNodeNames();

    /**
     * get public key create the given nodeName
     *
     * @param nodeName
     * @return
     */
    String pubKeyForConsensus(String nodeName);

    /**
     * get public key create the given nodeName
     *
     * @param nodeName
     * @return
     */
    String pubKeyForBiz(String nodeName);

    /**
     * get the self private key
     *
     * @return
     */
    String priKeyForConsensus();

    /**
     * get the self private key
     *
     * @return
     */
    String priKeyForBiz();

    /**
     * init the cluster info
     *
     * @param vo
     */
    void init(ClusterInfoVo vo);
}
