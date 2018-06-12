package com.higgs.trust.config.p2p;

import java.util.List;

/**
 * @author cwy
 */
public interface ClusterInfo {

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
    String pubKey(String nodeName);

    /**
     * get the self private key
     *
     * @return
     */
    String privateKey();
}
