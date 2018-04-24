package com.higgs.trust.consensus.p2pvalid.core.spi;

import java.util.List;

/**
 * @author cwy
 */
public interface ClusterInfo {

    /**
     * get self node name
     *
     * @return
     */
    String myNodeName();

    /**
     * get cluster nodeNames
     *
     * @return
     */
    List<String> clusterNodeNames();

    /**
     * get public key of the given nodeName
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
