package com.higgs.trust.slave.core.api.impl;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.contract.StateManager;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.common.context.AppContext;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.*;
import com.higgs.trust.slave.core.repository.account.CurrencyRepository;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.repository.contract.ContractStateRepository;
import com.higgs.trust.slave.core.service.datahandler.manage.SystemPropertyHandler;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXOSnapshotHandler;
import com.higgs.trust.slave.core.service.pending.PendingStateImpl;
import com.higgs.trust.slave.core.service.pending.TransactionValidator;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractStateSnapshotAgent;
import com.higgs.trust.slave.integration.block.BlockChainClient;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeAction;
import com.higgs.trust.slave.model.bo.contract.ContractState;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import com.higgs.trust.slave.model.enums.biz.TxSubmitResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static com.higgs.trust.consensus.config.NodeState.MASTER_NA;

/**
 * @author tangfashuang
 * @date 2918/04/14 16:52
 * @desc block chain service
 */
@Slf4j
@Service
public class BlockChainServiceImpl implements BlockChainService {

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

    @Autowired
    private DataIdentityRepository dataIdentityRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private UTXOSnapshotHandler utxoSnapshotHandler;

    @Autowired
    private SystemPropertyHandler systemPropertyHandler;

    @Autowired
    private PendingTxRepository pendingTxRepository;

    @Autowired
    private TransactionValidator transactionValidator;

    @Autowired
    private Executor txConsumerExecutor;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractStateRepository contractStateRepository;


    @Value("${trust.batch.tx.limit:200}")
    private int TX_PENDING_COUNT;

    @Override
    public RespData<List<TransactionVO>> submitTransactions(List<SignedTransaction> transactions) {
        RespData<List<TransactionVO>> respData = new RespData();
        if (!nodeState.isState(NodeStateEnum.Running)) {
            log.warn("the node status:{} is not Running please wait a later..", nodeState.getState());
            return respData;
        }
        if (CollectionUtils.isEmpty(transactions)) {
            log.error("received transaction list is empty");
            return new RespData(RespCodeEnum.PARAM_NOT_VALID);
        }

        if (StringUtils.equals(nodeState.getMasterName(), MASTER_NA)) {
            log.warn("cluster master is N/A");
            respData.setCode(RespCodeEnum.SYS_FAIL.getRespCode());
            respData.setData(buildTxVOList(transactions));
            return respData;
        }

        List<TransactionVO> transactionVOList = new ArrayList<>();
        List<SignedTransaction> newSignedTxList = new ArrayList<>();

        for (SignedTransaction signedTx : transactions) {
            TransactionVO transactionVO = new TransactionVO();
            String txId = signedTx.getCoreTx().getTxId();
            transactionVO.setTxId(txId);

            //verify params for transaction
            try {
                transactionValidator.verify(signedTx);
            } catch (SlaveException e) {
                transactionVO.setErrCode(e.getCode().getCode());
                transactionVO.setErrMsg(e.getCode().getDescription());
                transactionVO.setRetry(false);
                transactionVOList.add(transactionVO);
                continue;
            }
            newSignedTxList.add(signedTx);
        }

        newSignedTxList = checkDbIdempotent(newSignedTxList, transactionVOList);
        if (CollectionUtils.isEmpty(newSignedTxList)) {
            log.warn("all transactions idempotent");
            respData.setData(transactionVOList.size() > 0 ? transactionVOList : null);
            return respData;
        }

        RespData<List<TransactionVO>> masterResp = submitToMaster(newSignedTxList);
        if (null != masterResp.getData()) {
            transactionVOList.addAll(masterResp.getData());
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

        for (SignedTransaction signedTx : transactions) {
            String txId = signedTx.getCoreTx().getTxId();
            if (signedTxIds.contains(txId)) {
                signedTransactions.add(signedTx);
                signedTxIds.remove(txId);
            }
        }
        return signedTransactions;
    }

    /**
     * for performance test
     *
     * @param tx
     * @return
     */
    @Override
    public RespData<List<TransactionVO>> submitTransaction(SignedTransaction tx) {
        RespData<List<TransactionVO>> respData;
        //TODO 放到消费队列里面
        if (AppContext.PENDING_TO_SUBMIT_QUEUE.size() > Constant.MAX_PENDING_TX_QUEUE_SIZE) {
            log.warn("pending to submit queue is full, size={}", AppContext.PENDING_TO_SUBMIT_QUEUE.size());
            return new RespData(RespCodeEnum.SYS_FAIL);
        }

        AppContext.PENDING_TO_SUBMIT_QUEUE.offer(tx);

        respData = new RespData();
        respData.setCode(RespCodeEnum.SUCCESS.getRespCode());
        respData.setMsg("submitted, have " + AppContext.PENDING_TO_SUBMIT_QUEUE.size() + " transactions waiting to process ..." );
        return respData;
    }

    @Override
    public RespData<List<TransactionVO>> submitToMaster(List<SignedTransaction> transactions) {

        RespData<List<TransactionVO>> respData = new RespData();

        if (CollectionUtils.isEmpty(transactions)) {
            log.warn("transactions is empty");
            return respData;
        }

        List<TransactionVO> transactionVOList;

        // when master is running , then add txs into local pending txs
        if (nodeState.isMaster()) {
            if (nodeState.isState(NodeStateEnum.Running)) {
                log.debug("The node is master and it is running , add txs:{} into pending txs", transactions);
                transactionVOList = pendingState.addPendingTransactions(transactions);
            } else {
                log.debug("The node is master but the status is not running, cannot receive txs: {}", transactions);
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

    @Override
    public List<BlockHeader> listBlockHeaders(long startHeight, int size) {
        return blockRepository.listBlockHeaders(startHeight, size);
    }

    @Override
    public List<Block> listBlocks(long startHeight, int size) {
        return blockRepository.listBlocks(startHeight, size);
    }

    @Override
    public PageVO<BlockVO> queryBlocks(QueryBlockVO req) {
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

    @Override
    public PageVO<CoreTransactionVO> queryTransactions(QueryTransactionVO req) {

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

    @Override
    public List<UTXOVO> queryUTXOByTxId(String txId) {
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
    @Override
    public boolean isExistedIdentity(String identity) {
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
    @Override
    public boolean isExistedCurrency(String currency) {
        return currencyRepository.isExits(currency);
    }

    /**
     * query contract address by currency
     *
     * @return
     */
    @Override
    public String queryContractAddressByCurrency(String currency) {
        CurrencyInfo currencyInfo = currencyRepository.queryByCurrency(currency);
        if (null != currencyInfo) {
            return currencyInfo.getContractAddress();
        }
        return null;
    }


    /**
     * check whether the contract address is existed
     *
     * @param address
     * @return
     */
    @Override
    public boolean isExistedContractAddress(String address) {
        return contractRepository.isExistedAddress(address);
    }

    /**
     * query System Property by key
     *
     * @param key
     * @return
     */
    @Override
    public SystemPropertyVO querySystemPropertyByKey(String key) {
        return systemPropertyHandler.querySystemPropertyByKey(key);
    }

    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    @Override
    public List<UTXO> queryUTXOList(List<TxIn> inputList) {
        log.info("When process UTXO contract  querying queryTxOutList by inputList:{}", inputList);
        return utxoSnapshotHandler.queryUTXOList(inputList);
    }

    /**
     * get utxo action type
     *
     * @param name
     * @return
     */
    @Override
    public UTXOActionTypeEnum getUTXOActionType(String name) {
        return UTXOActionTypeEnum.getUTXOActionTypeEnumByName(name);
    }

    @Override
    public BlockHeader getBlockHeader(Long blockHeight) {
        return blockRepository.getBlockHeader(blockHeight);
    }

    @Override
    public BlockHeader getMaxBlockHeader() {
        return blockRepository.getBlockHeader(blockRepository.getMaxHeight());
    }

    @Override
    public Long getMaxBlockHeight() {
        return blockRepository.getMaxHeight();
    }

    @Override
    public List<BlockVO> queryBlocksByPage(QueryBlockVO req) {
        if (null == req) {
            return null;
        }
        //less than minimum，use default value
        Integer minNo = 0;
        if (null == req.getPageNo() || req.getPageNo().compareTo(minNo) <= 0) {
            req.setPageNo(1);
        }
        //over the maximum，use default value
        Integer maxSize = 100;
        if (null == req.getPageSize() || req.getPageSize().compareTo(maxSize) == 1) {
            req.setPageSize(20);
        }
        return blockRepository
                .queryBlocksWithCondition(req.getHeight(), req.getBlockHash(), req.getPageNo(), req.getPageSize());
    }

    @Override
    public List<CoreTransactionVO> queryTxsByPage(QueryTransactionVO req) {
        if (null == req) {
            return null;
        }
        //less than minimum，use default value
        Integer minNo = 0;
        if (null == req.getPageNo() || req.getPageNo().compareTo(minNo) <= 0) {
            req.setPageNo(1);
        }
        //over the maximum，use default value
        Integer maxSize = 100;
        if (null == req.getPageSize() || req.getPageSize().compareTo(maxSize) == 1) {
            req.setPageSize(20);
        }
        return transactionRepository
                .queryTxsWithCondition(req.getBlockHeight(), req.getTxId(), req.getSender(), req.getPageNo(),
                        req.getPageSize());
    }

    @Override
    public BlockVO queryBlockByHeight(Long height) {
        return blockRepository.queryBlockByHeight(height);
    }

    @Override
    public CoreTransactionVO queryTxById(String txId) {
        CoreTransactionVO vo = transactionRepository.queryTxById(txId);
        if (vo != null) {
            Object contractState = getContractState(txId, vo);
            vo.setContractState(contractState);
        }
        return vo;
    }

    @Override
    public List<CoreTransactionVO> queryTxByIds(List<String> txIds) {
        return transactionRepository.queryTxs(txIds);
    }

    /**
     * get contract state
     *
     * @param txId
     * @param vo
     * @return
     */
    private Object getContractState(String txId, CoreTransactionVO vo) {
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(vo.getPolicyId());
        if (initPolicyEnum != InitPolicyEnum.CONTRACT_ISSUE && initPolicyEnum != InitPolicyEnum.CONTRACT_INVOKE) {
            return null;
        }
        String address = null;
        if (initPolicyEnum == InitPolicyEnum.CONTRACT_ISSUE) {
            //query contract by txId and action index
            ContractVO contractVO = contractRepository.queryByTxId(txId, 0);
            if (contractVO == null) {
                log.info("[getContractState] get contract by txId:{} is null", txId);
                return null;
            }
            address = contractVO.getAddress();
        } else {
            String actionDatas = vo.getActionDatas();
            if (StringUtils.isEmpty(actionDatas)) {
                log.info("[getContractState] vo.getActionDatas is empty txId:{} ", txId);
                return null;
            }
            List<ContractInvokeAction> actions = JSON.parseArray(actionDatas, ContractInvokeAction.class);
            if (CollectionUtils.isEmpty(actions)) {
                log.info("[getContractState] parse actionDatas is empty txId:{} ", txId);
                return null;
            }
            ContractInvokeAction action = actions.get(0);
            if (action == null) {
                log.info("[getContractState] get ContractInvokeAction is null txId:{} ", txId);
                return null;
            }
            address = action.getAddress();
        }
        if (StringUtils.isEmpty(address)) {
            log.info("[getContractState] get contract address is null");
            return null;
        }
        //make new key
        String key = StateManager.makeStateKey(address, txId);
        //by md5
        key = ContractStateSnapshotAgent.makeNewKey(key);
        //query state by key
        ContractState contractState = contractStateRepository.getState(key);
        if (contractState == null) {
            return null;
        }
        return contractState.getState();
    }
}
