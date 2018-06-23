package com.higgs.trust.slave.core.service.ca;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.util.CaKeyLoader;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CaKeyLoaderImpl implements CaKeyLoader {

    @Autowired private CaRepository caRepository;
    @Autowired private ConfigRepository configRepository;
    @Autowired private NodeState nodeState;

    /** 
     * @desc acquire pubKey by nodeName
     * @param
     * @return   
     */  
    @Override public String loadPublicKey(String nodeName) throws Exception {
        return caRepository.getCa(nodeName).getPubKey();
    }

    /** 
     * @desc acquire current node priKey
     * @param
     * @return   
     */  
    @Override public String loadPrivateKey() throws Exception {
        return configRepository.getConfig(nodeState.getNodeName()).getPriKey();
    }
}
