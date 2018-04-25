package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.pending.PendingStateImpl;
import com.higgs.trust.slave.integration.block.BlockChainClient;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired private BlockRepository blockRepository;

    @Override public RespData submitTransaction(List<SignedTransaction> transactions) {
        RespData respData = new RespData();
        List<TransactionVO> transactionVOList = new ArrayList<>();
        // when master is running , then add txs into local pending txs
        if (nodeState.isMaster() && nodeState.isState(NodeStateEnum.Running)) {
                log.info("The node is master and it is running , add txs:{} into pending txs", transactions);
                transactionVOList = pendingState.addPendingTransactions(transactions);
        }

        //when it is not master ,then send txs to master node
        if(!nodeState.isMaster()) {
            //TODO test
            log.info("this node is not  master  , send txs:{} to master node", transactions);
            transactionVOList = blockChainClient.submitTransaction(nodeState.getMasterName(), transactions);
        }

        respData.setData(transactionVOList);
        return respData;
    }

    public List<BlockHeader> listBlockHeaders(long startHeight, int size) {
       return blockRepository.listBlockHeaders(startHeight, size);
    }

    public List<Block> listBlocks(long startHeight, int size) {
        return blockRepository.listBlocks(startHeight, size);
    }

}
