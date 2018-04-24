package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.service.pending.PendingStateImpl;
import com.higgs.trust.slave.integration.block.BlockChainClient;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tangfashuang
 * @date 2918/04/14 16:52
 * @desc block chain service
 */
@Slf4j @Service public class BlockChainServiceImpl implements BlockChainService {

    @Autowired private PendingStateImpl pendingState;

    @Autowired
    private NodeState nodeState;

    @Autowired
    private BlockChainClient blockChainClient;

    @Override public RespData submitTransaction(List<SignedTransaction> transactions) {
        RespData respData = new RespData();
        List<TransactionVO> transactionVOList;
        if (nodeState.isState(NodeStateEnum.Running)) {
            transactionVOList = pendingState.addPendingTransactions(transactions);
        } else {
            //TODO test
            transactionVOList = blockChainClient.submitTransaction(nodeState.getMasterName(), transactions);
        }

        respData.setData(transactionVOList);
        return respData;
    }
}
