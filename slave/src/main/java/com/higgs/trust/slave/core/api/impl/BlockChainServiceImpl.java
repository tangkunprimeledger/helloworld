package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.common.context.AppContext;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.DataIdentityRepository;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import com.higgs.trust.slave.core.repository.TxOutRepository;
import com.higgs.trust.slave.core.repository.account.CurrencyRepository;
import com.higgs.trust.slave.core.service.datahandler.manage.SystemPropertyHandler;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXOSnapshotHandler;
import com.higgs.trust.slave.core.service.pending.PendingStateImpl;
import com.higgs.trust.slave.integration.block.BlockChainClient;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
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

    private static final String MASTER_NA = "N/A";

    @Autowired private PendingStateImpl pendingState;

    @Autowired private NodeState nodeState;

    @Autowired private BlockChainClient blockChainClient;

    @Autowired private BlockRepository blockRepository;

    @Autowired private TransactionRepository transactionRepository;

    @Autowired private TxOutRepository txOutRepository;

    @Autowired private DataIdentityRepository dataIdentityRepository;

    @Autowired private CurrencyRepository currencyRepository;

    @Autowired private UTXOSnapshotHandler utxoSnapshotHandler;

    @Autowired private SystemPropertyHandler systemPropertyHandler;

    @Override public RespData submitTransactions(List<SignedTransaction> transactions) {
        RespData respData = new RespData();
        List<TransactionVO> transactionVOList;

        if (StringUtils.equals(nodeState.getMasterName(), MASTER_NA)) {
            log.warn("cluster master is N/A");
            respData.setCode(RespCodeEnum.SYS_FAIL.getRespCode());
            respData.setData(buildTxVOList(transactions));
            return respData;
        }

        // when master is running , then add txs into local pending txs
        if (nodeState.isMaster()) {
            if (nodeState.isState(NodeStateEnum.Running)) {
                if (log.isDebugEnabled()) {
                    log.debug("The node is master and it is running , add txs:{} into pending txs", transactions);
                }
                transactionVOList = pendingState.addPendingTransactions(transactions);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The node is master but the status is not running, cannot receive txs: {}", transactions);
                }
                transactionVOList = buildTxVOList(transactions);
            }
            respData.setData(transactionVOList);
        } else {
            if (log.isDebugEnabled()) {
                //when it is not master ,then send txs to master node
                log.debug("this node is not  master, send txs:{} to master node={}", transactions,
                    nodeState.getMasterName());
            }
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
        log.warn("master node status is not running. can not receive tx");
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

    @Override public PageVO<BlockVO> queryBlocks(QueryBlockVO req) {
        if (null == req) {
            return null;
        }
        if (null == req.getPageNo()) {
            req.setPageNo(1);
        }
        if (null == req.getPageSize()) {
            req.setPageSize(20);
        }
        PageVO<BlockVO> pageVO = new PageVO<>();
        pageVO.setPageNo(req.getPageNo());
        pageVO.setPageSize(req.getPageSize());

        long count = blockRepository.countBlocksWithCondition(req.getHeight(), req.getBlockHash());
        pageVO.setTotal(count);
        if (0 == count) {
            pageVO.setData(null);
        } else {
            List<BlockVO> list = blockRepository
                .queryBlocksWithCondition(req.getHeight(), req.getBlockHash(), req.getPageNo(), req.getPageSize());
            pageVO.setData(list);
        }

        log.info("[BlockChainServiceImpl.queryBlocks] query result: {}", pageVO);
        return pageVO;
    }

    @Override public PageVO<CoreTransactionVO> queryTransactions(QueryTransactionVO req) {

        if (null == req) {
            return null;
        }

        if (null == req.getPageNo()) {
            req.setPageNo(1);
        }
        if (null == req.getPageSize()) {
            req.setPageSize(200);
        }

        PageVO<CoreTransactionVO> pageVO = new PageVO<>();
        pageVO.setPageNo(req.getPageNo());
        pageVO.setPageSize(req.getPageSize());

        long count = transactionRepository.countTxsWithCondition(req.getBlockHeight(), req.getTxId(), req.getSender());
        pageVO.setTotal(count);
        if (0 == count) {
            pageVO.setData(null);
        } else {
            List<CoreTransactionVO> list = transactionRepository
                .queryTxsWithCondition(req.getBlockHeight(), req.getTxId(), req.getSender(), req.getPageNo(),
                    req.getPageSize());
            pageVO.setData(list);
        }

        log.info("[BlockChainServiceImpl.queryTransactions] query result: {}", pageVO);
        return pageVO;
    }

    @Override public List<UTXOVO> queryUTXOByTxId(String txId) {
        if (StringUtils.isBlank(txId)) {
            return null;
        }

        List<UTXOVO> list = txOutRepository.queryTxOutByTxId(txId);
        log.info("[BlockChainServiceImpl.queryUTXOByTxId] query result: {}", list);
        return list;
    }

    /**
     * check whether the identity is existed
     *
     * @param identity
     * @return
     */
    @Override public boolean isExistedIdentity(String identity) {
        if (StringUtils.isBlank(identity)) {
            return false;
        }
        return dataIdentityRepository.isExist(identity);
    }

    /**
     * check currency
     *
     * @param currency
     * @return
     */
    @Override public boolean isExistedCurrency(String currency) {
        return currencyRepository.isExits(currency);
    }

    /**
     * query System Property by key
     *
     * @param key
     * @return
     */
    @Override public SystemPropertyVO querySystemPropertyByKey(String key) {
        return systemPropertyHandler.querySystemPropertyByKey(key);
    }

    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    @Override public List<UTXO> queryUTXOList(List<TxIn> inputList) {
        log.info("When process UTXO contract  querying queryTxOutList by inputList:{}", inputList);
        return utxoSnapshotHandler.queryUTXOList(inputList);
    }

    /**
     * get utxo action type
     *
     * @param name
     * @return
     */
    @Override public UTXOActionTypeEnum getUTXOActionType(String name) {
        return UTXOActionTypeEnum.getUTXOActionTypeEnumByName(name);
    }

    @Override public BlockHeader getBlockHeader(Long blockHeight) {
        return blockRepository.getBlockHeader(blockHeight);
    }

    @Override public BlockHeader getMaxBlockHeader() {
        return blockRepository.getBlockHeader(blockRepository.getMaxHeight());
    }

}
