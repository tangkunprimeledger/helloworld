package com.higgs.trust.rs.core.service.rpc;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.network.NetworkManage;
import com.higgs.trust.rs.core.api.ContractV2QueryService;
import com.higgs.trust.rs.core.bo.ContractQueryStateV2BO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ContractV2 Query Client
 *
 * @author: lingchao
 * @datetime:2019-01-05 23:32
 **/
@ConditionalOnProperty(name = "higgs.trust.isSlave", havingValue = "false")
@Service
public class ContractV2QueryClient implements ContractV2QueryService {
    @Autowired
    private NetworkManage networkManage;
    @Autowired
    private NodeState nodeState;

    /**
     *
     * @param blockHeight
     * @param contractAddress
     * @param methodSignature
     * @param methodInputArgs
     * @return
     */
    @Override
    public List<?> query(Long blockHeight, String contractAddress, String methodSignature, Object... methodInputArgs) {
        ContractQueryStateV2BO contractQueryStateV2BO =  new ContractQueryStateV2BO(blockHeight, contractAddress, methodSignature, methodInputArgs);
        return networkManage.rpcClient().sendAndReceive(nodeState.getNodeName(), "/contract/v2/query", contractQueryStateV2BO);
    }

}
