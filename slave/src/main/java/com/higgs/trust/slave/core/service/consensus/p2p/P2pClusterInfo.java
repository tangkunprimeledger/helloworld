package com.higgs.trust.slave.core.service.consensus.p2p;

import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.slave.common.config.PropertiesConfig;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.RsPubKeyRepository;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: the p2p cluster info maintained by slave
 * @author: pengdi
 **/
@Service public class P2pClusterInfo implements ClusterInfo {

    @Autowired NodeState nodeState;

    @Autowired RsPubKeyRepository rsPubKeyRepository;

    @Autowired PropertiesConfig propertiesConfig;

    /**
     * get faultNode num
     *
     * @return
     */
    @Override public Integer faultNodeNum() {
        return propertiesConfig.getP2pFaultNodeNum();
    }

    @Override public String myNodeName() {
        return nodeState.getNodeName();
    }

    @Override public List<String> clusterNodeNames() {
        return rsPubKeyRepository.queryAllRsId();
    }

    @Override public String pubKey(String nodeName) {
        RsPubKey rsPubKey = rsPubKeyRepository.queryByRsId(nodeName);
        return rsPubKey.getPubKey();
    }

    @Override public String privateKey() {
        return nodeState.getPrivateKey();
    }
}
