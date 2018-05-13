package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.common.context.AppContext;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import com.higgs.trust.slave.core.repository.TxOutRepository;
import com.higgs.trust.slave.core.service.pending.PendingStateImpl;
import com.higgs.trust.slave.integration.block.BlockChainClient;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.enums.biz.TxSubmitResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private PendingStateImpl pendingState;

    @Autowired
    private NodeState nodeState;

    @Autowired
    private BlockChainClient blockChainClient;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TxOutRepository txOutRepository;

    @Override public RespData submitTransactions(List<SignedTransaction> transactions) {
        RespData respData = new RespData();
        List<TransactionVO> transactionVOList = new ArrayList<>();
        // when master is running , then add txs into local pending txs
        if (nodeState.isMaster()) {
            if(nodeState.isState(NodeStateEnum.Running)) {
                log.info("The node is master and it is running , add txs:{} into pending txs", transactions);
                transactionVOList = pendingState.addPendingTransactions(transactions);
            } else {
                log.info("The node is master but the status is not running, cannot receive txs: {}", transactions);
                transactionVOList = buildTxVOList(transactions);
            }
            respData.setData(transactionVOList);
        } else {
            //when it is not master ,then send txs to master node
            log.info("this node is not  master, send txs:{} to master node", transactions);
            respData = blockChainClient.submitTransaction(nodeState.getMasterName(), transactions);
        }

        return respData;
    }

    @Override public RespData submitTransaction(SignedTransaction tx) {
        List<SignedTransaction> transactions = new ArrayList<>();
        transactions.add(tx);
        RespData respData = submitTransactions(transactions);
        if (null == respData.getData()) {
            try {
                respData = AppContext.TX_HANDLE_RESULT_MAP.poll(tx.getCoreTx().getTxId(), 1000);
            } catch (InterruptedException e) {
                log.error("tx handle exception. ", e);
                respData = new RespData();
                respData.setCode(RespCodeEnum.SYS_FAIL.getRespCode());
                respData.setMsg("handle transaction exception.");
            }
        }

        if (null == respData) {
            respData = new RespData();
            respData.setCode(RespCodeEnum.SYS_HANDLE_TIMEOUT.getRespCode());
            respData.setMsg("tx handle timeout");
        }
        return respData;
    }

    private List<TransactionVO> buildTxVOList(List<SignedTransaction> transactions) {
        log.warn("master node status is not running. cannot receive tx");
        List<TransactionVO> transactionVOList = new ArrayList<>();
        transactions.forEach(signedTx -> {
            TransactionVO txVO = new TransactionVO();
            txVO.setErrCode(TxSubmitResultEnum.NODE_STATE_IS_NOT_RUNNING.getCode());
            txVO.setErrMsg(TxSubmitResultEnum.NODE_STATE_IS_NOT_RUNNING.getDesc());
            txVO.setTxId(signedTx.getCoreTx().getTxId());
            txVO.setRetry(true);
            transactionVOList.add(txVO);
        });

        return transactionVOList;
    }

    @Override public List<BlockHeader> listBlockHeaders(long startHeight, int size) {
       return blockRepository.listBlockHeaders(startHeight, size);
    }

    @Override public List<Block> listBlocks(long startHeight, int size) {
        return blockRepository.listBlocks(startHeight, size);
    }

    @Override public List<BlockVO> queryBlocks(QueryBlockVO req) {
        return blockRepository.queryBlocksWithCondition(req.getHeight(), req.getBlockHash(), req.getPageNum(), req.getPageSize());
    }

    @Override public List<CoreTransactionVO> queryTransactions(QueryTxVO req) {
        return transactionRepository.queryTxsWithCondition(req.getBlockHeight(), req.getTxId(), req.getSender(), req.getPageNum(), req.getPageSize());
    }

    @Override public List<UTXOVO> queryUTXOByTxId(String txId) {
        if (StringUtils.isBlank(txId)) {
            return null;
        }

        return txOutRepository.queryTxOutByTxId(txId);
    }

}
