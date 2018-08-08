package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.common.context.AppContext;
import com.higgs.trust.slave.core.repository.*;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author tangfashuang
 * @date 2918/04/14 16:52
 * @desc block chain service
 */
@Slf4j @Service public class BlockChainServiceImpl implements BlockChainService, InitializingBean {

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

    @Autowired private PendingTxRepository pendingTxRepository;

    @Autowired private Executor txConsumerExecutor;

    @Value("${trust.batch.tx.limit:200}") private int TX_PENDING_COUNT;

    @Value("${trust.sleep.submitToMaster:50}") private int SLEEP_FOR_SUBMIT_TO_MASTER;

    @Override public RespData submitTransactions(List<SignedTransaction> transactions) {
        RespData respData = new RespData();

        if (CollectionUtils.isEmpty(transactions)) {
            log.error("received transaction list is empty");
            return new RespData(RespCodeEnum.PARAM_NOT_VALID);
        }

        List<TransactionVO> transactionVOList = new ArrayList<>();

        if (StringUtils.equals(nodeState.getMasterName(), MASTER_NA)) {
            log.warn("cluster master is N/A");
            respData.setCode(RespCodeEnum.SYS_FAIL.getRespCode());
            respData.setData(buildTxVOList(transactions));
            return respData;
        }

        Profiler.start("submit transactions");

        //TODO 压测分析注释代码，需还原
//        Profiler.enter("check db idempotent start");
//        List<SignedTransaction> newSignedTxList = checkDbIdempotent(transactions, transactionVOList);
//        Profiler.release();
//        if (CollectionUtils.isEmpty(newSignedTxList)) {
//            log.warn("all transactions idempotent");
//            respData.setData(transactionVOList.size() > 0 ? transactionVOList : null);
//            return respData;
//        }

        Profiler.enter("submit to master");
        //TODO 压测分析代码，还原需修改transactions为newSignedTxList
        RespData masterResp = submitToMaster(transactions);
        if (null != masterResp.getData()) {
            transactionVOList.addAll((List<TransactionVO>)masterResp.getData());
        }
        Profiler.release();
        Profiler.release();

        if (Profiler.getDuration() > 0) {
            Profiler.logDump();
        }

        respData.setData(transactionVOList);
        return respData;
    }

    private List<SignedTransaction> checkDbIdempotent(List<SignedTransaction> transactions,
        List<TransactionVO> transactionVOList) {

        List<SignedTransaction> signedTransactions = new ArrayList<>();

        //build tx_id set
        List<String> signedTxIds = new ArrayList<>();
        for (SignedTransaction signedTx : transactions) {
            signedTxIds.add(signedTx.getCoreTx().getTxId());
        }

        Profiler.enter("transaction idempotent");
        //check transaction db
        List<String> txIds = transactionRepository.queryTxIdsByIds(signedTxIds);
        if (!CollectionUtils.isEmpty(txIds)) {
            for (String txId : txIds) {
                log.warn("transaction idempotent, txId={}", txId);
                TransactionVO transactionVO = new TransactionVO();
                transactionVO.setTxId(txId);
                transactionVO.setErrCode(TxSubmitResultEnum.TX_IDEMPOTENT.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.TX_IDEMPOTENT.getDesc());
                transactionVO.setRetry(true);
                transactionVOList.add(transactionVO);

                //remove tx_id
                signedTxIds.remove(txId);
            }
        }

        Profiler.release();

        Profiler.enter("pending transaction idempotent");
        //check pending_transaction db
        List<String> pTxIds = pendingTxRepository.queryTxIds(signedTxIds);
        if (!CollectionUtils.isEmpty(pTxIds)) {
            for (String txId : pTxIds) {
                log.warn("pending transaction table idempotent, txId={}", txId);
                TransactionVO transactionVO = new TransactionVO();
                transactionVO.setTxId(txId);
                transactionVO.setErrCode(TxSubmitResultEnum.PENDING_TX_IDEMPOTENT.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.PENDING_TX_IDEMPOTENT.getDesc());
                transactionVO.setRetry(true);
                transactionVOList.add(transactionVO);

                //remove tx_id
                signedTxIds.remove(pTxIds);
            }
        }

        Profiler.release();

        Profiler.enter("build signedTx");
        for (SignedTransaction signedTx : transactions) {
            if (signedTxIds.contains(signedTx.getCoreTx().getTxId())) {
                signedTransactions.add(signedTx);
            }
        }
        Profiler.release();
        return signedTransactions;
    }

    /**
     * for performance test
     * @param tx
     * @return
     */
    @Override public RespData submitTransaction(SignedTransaction tx) {
        //TODO for load test
        log.info("accept tx with thread: " + Thread.currentThread().getName());

        RespData respData;
        //TODO 放到消费队列里面
        if (AppContext.PENDING_TO_SUBMIT_QUEUE.size() > Constant.MAX_PENDING_TX_QUEUE_SIZE) {
            log.warn("pending to submit queue is full, size={}", AppContext.PENDING_TO_SUBMIT_QUEUE.size());
            return new RespData(RespCodeEnum.SYS_FAIL);
        }

        AppContext.PENDING_TO_SUBMIT_QUEUE.offer(tx);
        try {
            respData = AppContext.TX_HANDLE_RESULT_MAP.poll(tx.getCoreTx().getTxId(), 1000);
        } catch (InterruptedException e) {
            log.error("tx handle exception. ", e);
            respData = new RespData();
            respData.setCode(RespCodeEnum.SYS_FAIL.getRespCode());
            respData.setMsg("handle transaction exception.");
        }

        if (null == respData) {
            respData = new RespData();
            respData.setCode(RespCodeEnum.SYS_HANDLE_TIMEOUT.getRespCode());
            respData.setMsg("tx handle timeout");
        }
        return respData;
    }

    @Override public RespData submitToMaster(List<SignedTransaction> transactions) {

        Profiler.start("submit to master");
        RespData respData = new RespData();

        if (CollectionUtils.isEmpty(transactions)) {
            log.warn("transactions is empty");
            return respData;
        }

        List<TransactionVO> transactionVOList;

        // when master is running , then add txs into local pending txs
        if (nodeState.isMaster()) {
            Profiler.enter("submit transactions to self");
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
            Profiler.release();
        } else {
            if (log.isDebugEnabled()) {
                //when it is not master ,then send txs to master node
                log.debug("this node is not  master, send txs:{} to master node={}", transactions,
                    nodeState.getMasterName());
            }
            Profiler.enter("submit transactions to master node");
            respData = blockChainClient.submitToMaster(nodeState.getMasterName(), transactions);
            Profiler.release();
        }

        Profiler.release();
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

    @Override public void afterPropertiesSet() throws Exception {
        txConsumerExecutor.execute(new ConsumerTx());
    }

    /**
     * for test
     */
    private class ConsumerTx implements Runnable {

        @Override public void run() {
            while (true) {
                try {
                    Thread.sleep(SLEEP_FOR_SUBMIT_TO_MASTER);

                    if (null == AppContext.PENDING_TO_SUBMIT_QUEUE.peek()) {
                        log.debug("queue is empty");
                        continue;
                    } else {
                        submit();
                    }
                } catch (InterruptedException e) {
                    log.error("Consumer tx thread InterruptedException");
                } catch (Throwable e) {
                    log.error("Consumer tx thread handle exception, ", e);
                }
            }
        }

        private void submit() {
            int i = 0;
            List<SignedTransaction> signedTxList = new ArrayList<>();
            log.info("pending transaction to submit, size={}", AppContext.PENDING_TO_SUBMIT_QUEUE.size());
            Profiler.start("start submit transactions");
            Profiler.enter("build transactionList");
            while (i++ < TX_PENDING_COUNT && (null != AppContext.PENDING_TO_SUBMIT_QUEUE.peek())) {
                signedTxList.add(AppContext.PENDING_TO_SUBMIT_QUEUE.poll());
            }
            Profiler.release();

            log.info("submit transactions, size={}", signedTxList.size());
            Profiler.enter("submit transactions");
            submitTransactions(signedTxList);
            Profiler.release();

            Profiler.release();

            if (Profiler.getDuration() > 0) {
                Profiler.logDump();
            }
        }
    }

}
