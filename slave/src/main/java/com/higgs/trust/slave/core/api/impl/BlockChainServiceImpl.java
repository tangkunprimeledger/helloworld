package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.common.constant.LoggerName;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    @Value("${trust.batch.tx.limit:200}") private int TX_PENDING_COUNT;

    @Value("${trust.sleep.submitToMaster:50}") private int SLEEP_FOR_SUBMIT_TO_MASTER;

    private static final Logger PERF_LOGGER = LoggerFactory.getLogger(LoggerName.PERF_LOGGER);

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

        List<SignedTransaction> newSignedTxList = checkDbIdempotent(transactions, transactionVOList);
        if (CollectionUtils.isEmpty(newSignedTxList)) {
            log.warn("all transactions idempotent");
            respData.setData(transactionVOList.size() > 0 ? transactionVOList : null);
            return respData;
        }

        RespData masterResp = submitToMaster(newSignedTxList);
        if (null != masterResp.getData()) {
            transactionVOList.addAll((List<TransactionVO>)respData.getData());
        }

        respData.setData(transactionVOList);
        return respData;
    }

    private List<SignedTransaction> checkDbIdempotent(List<SignedTransaction> transactions, List<TransactionVO> transactionVOList) {

        List<SignedTransaction> signedTransactions = new ArrayList<>();

        for (SignedTransaction signedTx : transactions) {
            TransactionVO transactionVO = new TransactionVO();
            String txId = signedTx.getCoreTx().getTxId();
            transactionVO.setTxId(txId);

            // chained transaction idempotent check, need retry
            if (transactionRepository.isExist(txId)) {
                log.warn("transaction idempotent, txId={}", txId);
                transactionVO.setErrCode(TxSubmitResultEnum.TX_IDEMPOTENT.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.TX_IDEMPOTENT.getDesc());
                transactionVO.setRetry(true);
                transactionVOList.add(transactionVO);
                continue;
            }

            // pending transaction idempotent check, need retry
            if (pendingTxRepository.isExist(txId)) {
                log.warn("pending transaction table idempotent, txId={}", txId);
                transactionVO.setErrCode(TxSubmitResultEnum.PENDING_TX_IDEMPOTENT.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.PENDING_TX_IDEMPOTENT.getDesc());
                transactionVO.setRetry(true);
                transactionVOList.add(transactionVO);
                continue;
            }
            signedTransactions.add(signedTx);
        }
        return signedTransactions;
    }

    /**
     * for performance test
     * @param tx
     * @return
     */
    @Override public RespData submitTransaction(SignedTransaction tx) {

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
        RespData respData = new RespData();

        if (CollectionUtils.isEmpty(transactions)) {
            log.warn("transactions is empty");
            return respData;
        }

        List<TransactionVO> transactionVOList;

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
            respData = blockChainClient.submitToMaster(nodeState.getMasterName(), transactions);
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

    @Override public void afterPropertiesSet() throws Exception {
        new ConsumerTx("consumerTx").start();
    }

    /**
     * for test
     */
    private class ConsumerTx extends Thread {

        public ConsumerTx(String name) {
            super.setName(name);
        }

        @Override public void run() {
            log.info("thread.name={}", Thread.currentThread().getName());
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
                PERF_LOGGER.info(Profiler.dump());
            }
        }
    }

}
