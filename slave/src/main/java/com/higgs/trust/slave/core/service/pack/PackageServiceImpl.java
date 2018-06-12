package com.higgs.trust.slave.core.service.pack;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.Labels;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.api.SlaveCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackRegistor;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.context.AppContext;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.repository.RsNodeRepository;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.consensus.log.LogReplicateHandler;
import com.higgs.trust.slave.core.service.consensus.p2p.P2pHandler;
import com.higgs.trust.slave.core.service.pending.PendingState;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.transaction.TransactionExecutor;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.context.PackageData;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import com.higgs.trust.slave.model.convert.PackageConvert;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: package service
 * @author: pengdi
 **/
@Service @Slf4j public class PackageServiceImpl implements PackageService {

    @Autowired private PackageRepository packageRepository;

    @Autowired private BlockRepository blockRepository;

    @Autowired private BlockService blockService;

    @Autowired private PendingState pendingState;

    @Autowired private NodeState nodeState;

    @Autowired private LogReplicateHandler logReplicateHandler;

    @Autowired private TransactionTemplate txRequired;

    @Autowired private TransactionExecutor transactionExecutor;

    @Autowired private SnapshotService snapshotService;

    @Autowired private TransactionRepository transactionRepository;

    @Autowired private SlaveCallbackRegistor slaveCallbackRegistor;

    @Autowired private P2pHandler p2pHandler;

    @Autowired private RsNodeRepository rsNodeRepository;

    @Value("${trust.package.pending:1000}")
    private int PACKAGE_PENDING_COUNT;

    /**
     * create new package from pending transactions
     *
     * @return
     */
    @Override public Package create(List<SignedTransaction> signedTransactions, Long packHeight) {

        if (CollectionUtils.isEmpty(signedTransactions)) {
            return null;
        }

        Long height = getHeight(packHeight);

        if (null == height) {
            //write list of SignedTransaction to pendingTxQueue
            pendingState.addPendingTxsToQueueFirst(signedTransactions);
            return null;
        }

        log.info("[PackageServiceImpl.createPackage] start create package, txSize: {}, txList: {}, package.height: {}",
            signedTransactions.size(), signedTransactions, height + 1);

        /**
         * initial package
         */
        Package pack = new Package();
        pack.setSignedTxList(signedTransactions);
        pack.setPackageTime(System.currentTimeMillis());

        //get max height, add 1 for next package height
        pack.setHeight(height + 1);
        //set status = INIT
        pack.setStatus(PackageStatusEnum.INIT);
        return pack;
    }

    /**
     * get maxBlockHeight from db, packHeight from memory.
     * if maxBlockHeight is null, log error, return null.
     * if packHeight is null, return maxBlockHeight.(if exchange master, maxPackHeight must be initialized)
     * if package is null which height = packHeight, then return null
     * else return packHeight
     * @return
     */
    private Long getHeight(Long packHeight) {
        Long maxBlockHeight = blockRepository.getMaxHeight();

        //genius block must be exist
        if (null == maxBlockHeight) {
            log.error("please initialize genius block");
            return null;
        }

        //when exchange master, maxPackHeight must be initialized
        if (null == packHeight) {
            return maxBlockHeight;
        }

        if (null == packageRepository.load(packHeight)) {
            return null;
        }

        return packHeight;
    }

    @Override public void submitConsensus(Package pack) {
        PackageVO packageVO = PackageConvert.convertPackToPackVO(pack);
        logReplicateHandler.replicatePackage(packageVO);
    }

    /**
     * receive new package from somewhere, almost from consensus
     *
     * @param pack
     */
    @Override public void receive(Package pack) {
        log.info("receive package from consensus, pack: {}", pack);

        if (null == pack || CollectionUtils.isEmpty(pack.getSignedTxList())) {
            log.error("package is null or transaction list is empty.");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        if (!checkTransactions(pack.getSignedTxList())) {
            log.error("transaction list is not order by txId asc.");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        Package packageBO = packageRepository.load(pack.getHeight());
        // check package hash
        if (null != packageBO) {
            boolean checkHash = StringUtils.equals(buildPackHash(pack), buildPackHash(packageBO));
            if (!checkHash) {
                log.error("receive package is not the same as db package");
                //TODO 添加告警
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            }
        }

        pack.setStatus(PackageStatusEnum.RECEIVED);
        packageRepository.save(pack);
    }

    /**
     * check transaction list if sorted by txId
     *
     * @param signedTxList
     * @return
     */
    private boolean checkTransactions(List<SignedTransaction> signedTxList) {
        for (int i = 0; i < signedTxList.size() - 1; i++) {
            String firstTxId = signedTxList.get(i).getCoreTx().getTxId();
            String secondTxId = signedTxList.get(i + 1).getCoreTx().getTxId();
            if (firstTxId.compareTo(secondTxId) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * create pack context for main process
     *
     * @param pack
     * @return
     */
    @Override public PackContext createPackContext(Package pack) {
        Block block = blockService.buildDummyBlock(pack.getHeight(), pack.getPackageTime());

        PackContext packContext = new PackContext(pack, block);
        //set rsId and public key map
        List<RsPubKey> rsPubKeyList = rsNodeRepository.queryRsAndPubKey();
        if (!CollectionUtils.isEmpty(rsPubKeyList)) {
            packContext.setRsPubKeyMap(rsPubKeyList.stream().collect(Collectors.toMap(RsPubKey::getRsId, RsPubKey::getPubKey)));
        }

        return packContext;
    }


    /**
     * execute package persisting, get persist result and submit consensus layer
     *
     * @param packContext
     */
    @Override public void process(PackContext packContext) {
        log.info("process package start, package context: {}", packContext);
        Profiler.start("[PackageService.process.monitor]");
        Package pack = packContext.getCurrentPackage();
        List<SignedTransaction> txs = pack.getSignedTxList();
        if (CollectionUtils.isEmpty(txs)) {
            log.error("[package.process]the transactions in the package is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TXS_IS_EMPTY_ERROR);
        }

        try {
            //snapshot transactions should be init
            snapshotService.destroy();
            Profiler.enter("[execute txs]");
            //persist all transactions
            List<TransactionReceipt> txReceipts = executeTransactions(packContext);
            Profiler.release();
            Profiler.enter("[build block header]");
            //build a new block hash from db datas
            BlockHeader dbHeader = blockService.buildHeader(packContext, txReceipts);
            Profiler.enter("[persist block]");
            //build block and save to context
            Block block = blockService.buildBlock(packContext, dbHeader);
            packContext.setCurrentBlock(block);
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                    //persist block
                    blockService.persistBlock(block, txReceipts);
                }
            });
            Profiler.release();
            //call back business
            Profiler.enter("[callbackRSForPersisted]");
            callbackRS(block.getSignedTxList(), txReceipts,false);
            Profiler.release();

            //persist package
            Profiler.enter("[persist package]");
            packageRepository.updateStatus(pack.getHeight(), PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
            Profiler.release();

            //submit blockHeader to p2p
            Profiler.enter("[submit block header to p2p]");
            p2pHandler.sendPersisting(dbHeader);
            Profiler.release();
        } catch (Throwable e) {
            snapshotService.clear();
            log.error("[package.process]has unknown error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_PERSISTING_ERROR, e);
        } finally {
            //snapshot transactions should be destroy
            snapshotService.destroy();
            Profiler.release();
            Profiler.logDump();
        }

        pack.getSignedTxList().forEach(signedTx -> {
            try {
                AppContext.TX_HANDLE_RESULT_MAP.put(signedTx.getCoreTx().getTxId(), new RespData());
            } catch (InterruptedException e) {
                log.error("interrupted exception. txId={}", signedTx.getCoreTx().getTxId());
            }
        });
        log.info("process package finish");
    }

    /**
     * loop validate all transactions
     *
     * @param packageData
     * @return
     */
    private List<TransactionReceipt> executeTransactions(PackageData packageData) {
        List<SignedTransaction> txs = packageData.getCurrentPackage().getSignedTxList();
        Profiler.enter("[queryTxIds]");
        List<String> dbTxs = transactionRepository.queryTxIds(txs);
        Profiler.release();
        List<SignedTransaction> persistedDatas = new ArrayList<>();
        List<TransactionReceipt> txReceipts = new ArrayList<>(txs.size());
        //loop validate each transaction
        for (SignedTransaction tx : txs) {
            String title = new StringBuffer("[execute tx ").append(tx.getCoreTx().getTxId()).append("]").toString();
            Profiler.enter(title);
            try {
                //ignore idempotent transaction
                if (hasTx(dbTxs, tx.getCoreTx().getTxId())) {
                    log.info("{} is idempotent", tx.getCoreTx().getTxId());
                    continue;
                }
                //set current transaction and execute
                packageData.setCurrentTransaction(tx);
                TransactionReceipt receipt = transactionExecutor.process(packageData.parseTransactionData(), packageData.getRsPubKeyMap());
                persistedDatas.add(tx);
                txReceipts.add(receipt);
            } finally {
                Profiler.release();
            }
        }
        packageData.getCurrentBlock().setSignedTxList(persistedDatas);
        return txReceipts;
    }

    /**
     * check tx is exist by txId
     *
     * @param txs
     * @param txId
     * @return
     */
    private boolean hasTx(List<String> txs, String txId) {
        if (CollectionUtils.isEmpty(txs)) {
            return false;
        }
        for (String mTxId : txs) {
            if (StringUtils.equals(txId, mTxId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * receive persist final result from consensus layer
     *
     * @param header
     */
    @Override
    public void persisted(BlockHeader header) {
        /**
         * TODO
         * 1.开启事务
         * 2.通过packageRepository更新package的状态为persisted
         * 3.通过pendingState触发业务RS的callback操作
         * 4.提交事务
         */
        Profiler.start("[PackageService.persisted] is start");
        Profiler.enter("[start query temp header of CONSENSUS_VALIDATE_TYPE]");
        //gets the block header from db
        BlockHeader blockHeader =
            blockService.getHeader(header.getHeight());
        Profiler.release();
        //check hash
        if (blockHeader == null) {
            log.warn("[package.persisted] consensus header of db is null blockHeight:{}", header.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
        }

        //compare
        boolean r = blockService.compareBlockHeader(blockHeader, header);
        if (!r) {
            log.error("[package.persisted] consensus header unequal tempHeader,blockHeight:{}", header.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TWO_HEADER_UNEQUAL_ERROR);
        }
        try {
            //call back business
            Profiler.enter("[callbackRSForClusterPersisted]");
            List<SignedTransaction> txs = transactionRepository.queryTransactions(header.getHeight());
            List<TransactionReceipt> txReceipts = transactionRepository.queryTxReceipts(txs);
            callbackRS(txs, txReceipts,true);
            Profiler.release();

            //update package status ---- PERSISTED
            Profiler.enter("[updatePackStatus]");
            packageRepository.updateStatus(blockHeader.getHeight(),
                PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
            Profiler.release();

        }catch (Throwable e){
            log.error("[package.persisted]callback rs has error", e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_CALLBACK_ERROR,e);
        }finally {
            Profiler.release();
            if (Profiler.getDuration() > 0) {
                Profiler.logDump();
            }
        }
    }

    /**
     * call back business
     */
    private void callbackRS(List<SignedTransaction> txs, List<TransactionReceipt> txReceipts,boolean isClusterPersisted) {
        log.info("[callbackRS]isClusterPersisted:{}",isClusterPersisted);
        SlaveCallbackHandler callbackHandler = slaveCallbackRegistor.getSlaveCallbackHandler();
        if (callbackHandler == null) {
            log.warn("[callbackRS]callbackHandler is not register");
            //throw new SlaveException(SlaveErrorEnum.SLAVE_RS_CALLBACK_NOT_REGISTER_ERROR);
            return;
        }
        if (CollectionUtils.isEmpty(txs)) {
            log.error("[callbackRS]txs is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TXS_IS_EMPTY_ERROR);
        }
        for (SignedTransaction tx : txs) {
            String txId = tx.getCoreTx().getTxId();
            RespData<CoreTransaction> respData = new RespData<>();
            respData.setData(tx.getCoreTx());
            //make resp code and msg for fail tx
            for (TransactionReceipt receipt : txReceipts) {
                if (StringUtils.equals(receipt.getTxId(), txId) && !receipt.isResult()) {
                    respData.setCode(receipt.getErrorCode());
                    SlaveErrorEnum slaveErrorEnum = SlaveErrorEnum.getByCode(receipt.getErrorCode());
                    if (slaveErrorEnum != null) {
                        respData.setMsg(slaveErrorEnum.getDescription());
                    }
                    break;
                }
            }
            //callback business
            log.info("[callbackRS]start callback rs txId:{}", txId);
            if(isClusterPersisted){
                callbackHandler.onClusterPersisted(respData,tx.getSignatureList());
            }else{
                callbackHandler.onPersisted(respData,tx.getSignatureList());
            }
            log.info("[callbackRS]end callback rs txId:{}", txId);
        }
    }

    /**
     * remove the package if it is done
     *
     * @param pack
     */
    @Override public void remove(Package pack) {
        /** TODO
         * 1.判断package是否执行完成
         * 2.开启事务
         * 3.通过packageRepository删除包
         * 4.通过pendingState删除交易
         * 5.提交事务
         */
    }

    /**
     * build hash for package
     *
     * @param pack
     * @return
     */
    private String buildPackHash(Package pack) {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(function.hashLong(pack.getHeight()));
        builder.append(function.hashLong(pack.getPackageTime()));
        builder.append(function.hashString(getSafety(JSON.toJSONString(pack.getSignedTxList())), Charsets.UTF_8));
        String hash = function.hashString(builder.toString(), Charsets.UTF_8).toString();
        return hash;
    }

    private String getSafety(String data) {
        if (data == null) {
            return "";
        }
        return data;
    }

    /**
     * package status change function
     *
     * @param pack
     * @param from
     * @param to
     */
    @Override public void statusChange(Package pack, PackageStatusEnum from, PackageStatusEnum to) {
        packageRepository.updateStatus(pack.getHeight(), from, to);
    }
}
