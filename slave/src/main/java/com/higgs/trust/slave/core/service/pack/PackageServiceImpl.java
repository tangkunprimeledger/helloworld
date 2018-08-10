package com.higgs.trust.slave.core.service.pack;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.slave.api.SlaveBatchCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackRegistor;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.context.AppContext;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.repository.*;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.consensus.log.LogReplicateHandler;
import com.higgs.trust.slave.core.service.consensus.p2p.P2pHandler;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.transaction.TransactionExecutor;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.context.PackageData;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import com.higgs.trust.slave.model.convert.PackageConvert;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import com.higgs.trust.slave.model.enums.biz.PendingTxStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: package service
 * @author: pengdi
 **/
@Service @Slf4j public class PackageServiceImpl implements PackageService {
    @Autowired private TransactionTemplate txRequired;
    @Autowired private PackageRepository packageRepository;
    @Autowired private BlockRepository blockRepository;
    @Autowired private BlockService blockService;
    @Autowired private LogReplicateHandler logReplicateHandler;
    @Autowired private TransactionExecutor transactionExecutor;
    @Autowired private SnapshotService snapshotService;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SlaveCallbackRegistor slaveCallbackRegistor;
    @Autowired private P2pHandler p2pHandler;
    @Autowired private RsNodeRepository rsNodeRepository;
    @Autowired private PendingTxRepository pendingTxRepository;
    @Autowired private NodeState nodeState;

    /**
     * create new package from pending transactions
     *
     * @return
     */
    @Override public Package create(List<SignedTransaction> signedTransactions, Long currentPackageHeight) {

        if (CollectionUtils.isEmpty(signedTransactions)) {
            return null;
        }

        if (null == currentPackageHeight) {
            return null;
        }

        // sort signedTransactions by txId asc
        Collections.sort(signedTransactions, new Comparator<SignedTransaction>() {
            @Override public int compare(SignedTransaction signedTx1, SignedTransaction signedTx2) {
                return signedTx1.getCoreTx().getTxId().compareTo(signedTx2.getCoreTx().getTxId());
            }
        });

        log.info("[PackageServiceImpl.createPackage] start create package, txSize: {}, package.height: {}",
            signedTransactions.size(), currentPackageHeight + 1);

        /**
         * initial package
         */
        Package pack = new Package();
        pack.setSignedTxList(signedTransactions);
        pack.setPackageTime(System.currentTimeMillis());
        //set status = INIT
        pack.setStatus(PackageStatusEnum.INIT);
        return pack;
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
        log.info("receive package from consensus, pack height: {}", pack.getHeight());

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
                log.error("receive package is not the same as db package. height={}", pack.getHeight());
                MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_PACKAGE_HASH_NOT_EQUAL.getMonitorTarget(), 1);
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            } else {
                log.info("receive package is the same as db package. height={}", pack.getHeight());
                return;
            }
        }
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                pack.setStatus(PackageStatusEnum.RECEIVED);
                packageRepository.save(pack);
                //save pendingTx to db
                pendingTxRepository.batchInsert(pack.getSignedTxList(), PendingTxStatusEnum.PACKAGED, pack.getHeight());
            }
        });
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
        preparePackContext(packContext);

        return packContext;
    }

    /**
     * prepare package context
     *
     * @param packContext
     */
    private void preparePackContext(PackContext packContext) {
        //set rsId and public key map
        List<RsPubKey> rsPubKeyList = rsNodeRepository.queryRsAndPubKey();
        if (!CollectionUtils.isEmpty(rsPubKeyList)) {
            packContext.setRsPubKeyMap(
                rsPubKeyList.stream().collect(Collectors.toMap(RsPubKey::getRsId, RsPubKey::getPubKey)));
        }
    }

    /**
     * execute package persisting, get persist result and submit consensus layer
     *
     * @param packContext
     * @param isFailover
     * @param isBatchSync
     */
    @Override public void process(PackContext packContext, boolean isFailover, boolean isBatchSync) {
        log.info("process package start");
        Package pack = packContext.getCurrentPackage();
        List<SignedTransaction> txs = pack.getSignedTxList();
        if (CollectionUtils.isEmpty(txs)) {
            log.error("[package.process]the transactions in the package is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TXS_IS_EMPTY_ERROR);
        }
        Profiler.start("[PackageService.process.monitor]size:" + txs.size());
        try {
            //snapshot transactions should be init
            snapshotService.clear();

            Profiler.enter("[execute txs]");
            //persist all transactions
            List<TransactionReceipt> txReceipts = executeTransactions(packContext);
            Profiler.release();

            Profiler.enter("[build block header]");
            //build a new block hash from db datas
            BlockHeader dbHeader = blockService.buildHeader(packContext, txReceipts);
            Profiler.release();

            //build block and save to context
            Block block = blockService.buildBlock(packContext, dbHeader);
            packContext.setCurrentBlock(block);

            Profiler.enter("[snapshot flush]");
            snapshotService.flush();
            Profiler.release();

            //persist block
            Profiler.enter("[block flush]");
            blockService.persistBlock(block, txReceipts);
            Profiler.release();

            //call back business
            Profiler.enter("[callbackRS]");
            callbackRS(block.getSignedTxList(), txReceipts, false, isFailover, dbHeader);
            Profiler.release();

            if (!isBatchSync) {
                PackageStatusEnum from = PackageStatusEnum.RECEIVED;
                if (pack.getStatus() == PackageStatusEnum.FAILOVER) {
                    from = PackageStatusEnum.FAILOVER;
                }
                packageRepository.updateStatus(pack.getHeight(), from, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
                p2pHandler.sendPersisting(dbHeader);
            }
        } catch (Throwable e) {
            //snapshot transactions should be destroy
            snapshotService.destroy();
            log.error("[package.process]has unknown error");
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_PACKAGE_PROCESS_ERROR.getMonitorTarget(), 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_PERSISTING_ERROR, e);
        } finally {
            Profiler.release();
            //print if lager than 300 ms
            if (Profiler.getDuration() > Constant.PERF_LOG_THRESHOLD) {
                Profiler.logDump();
            }
        }
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
                TransactionReceipt receipt =
                    transactionExecutor.process(packageData.parseTransactionData(), packageData.getRsPubKeyMap());
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
    @Override public void persisted(BlockHeader header) {
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
        BlockHeader blockHeader = blockService.getHeader(header.getHeight());
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
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_BLOCK_HEADER_NOT_EQUAL.getMonitorTarget(), 1);
            //change state to offline
            nodeState.changeState(nodeState.getState(), NodeStateEnum.Offline);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TWO_HEADER_UNEQUAL_ERROR);
        }
        try {
            Profiler.enter("[queryTransactions]");
            List<SignedTransaction> txs = transactionRepository.queryTransactions(header.getHeight());
            if (!CollectionUtils.isEmpty(txs)) {
                // sort signedTransactions by txId asc
                Collections.sort(txs, new Comparator<SignedTransaction>() {
                    @Override public int compare(SignedTransaction signedTx1, SignedTransaction signedTx2) {
                        return signedTx1.getCoreTx().getTxId().compareTo(signedTx2.getCoreTx().getTxId());
                    }
                });
            }
            List<TransactionReceipt> txReceipts = transactionRepository.queryTxReceipts(txs);
            Profiler.release();

            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                    //call back business
                    Profiler.enter("[callbackRSForClusterPersisted]");
                    callbackRS(txs, txReceipts, true, false, blockHeader);
                    Profiler.release();
                    //check status for package
                    boolean isPackageStatus = packageRepository
                        .isPackageStatus(blockHeader.getHeight(), PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
                    if (!isPackageStatus) {
                        log.warn("[package.persisted]package status is not WAIT_PERSIST_CONSENSUS blockHeight:{}",
                            blockHeader.getHeight());
                        return;
                    }
                    //update package status ---- PERSISTED
                    Profiler.enter("[updatePackStatus]");
                    packageRepository.updateStatus(blockHeader.getHeight(), PackageStatusEnum.WAIT_PERSIST_CONSENSUS,
                        PackageStatusEnum.PERSISTED);
                    Profiler.release();
                }
            });
        } catch (SlaveException e) {
            throw e;
        } catch (Throwable e) {
            log.error("[package.persisted]callback rs has error", e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_CALLBACK_ERROR, e);
        } finally {
            Profiler.release();
            if (Profiler.getDuration() > Constant.PERF_LOG_THRESHOLD) {
                Profiler.logDump();
            }
        }
    }

    /**
     * call back business
     */
    private void callbackRS(List<SignedTransaction> txs, List<TransactionReceipt> txReceipts,
        boolean isClusterPersisted, boolean isFailover, BlockHeader blockHeader) {
        if (log.isDebugEnabled()) {
            log.debug("[callbackRS]isClusterPersisted:{}", isClusterPersisted);
        }
        if (CollectionUtils.isEmpty(txs)) {
            log.warn("[callbackRS]txs is empty");
            return;
        }
        SlaveCallbackHandler callbackHandler = slaveCallbackRegistor.getSlaveCallbackHandler();
        SlaveBatchCallbackHandler batchCallbackHandler = null;
        if (callbackHandler == null) {
            log.warn("[callbackRS]callbackHandler is not register");
            //throw new SlaveException(SlaveErrorEnum.SLAVE_RS_CALLBACK_NOT_REGISTER_ERROR);
            batchCallbackHandler = slaveCallbackRegistor.getSlaveBatchCallbackHandler();
            if (batchCallbackHandler == null) {
                log.warn("[callbackRS]batchCallbackHandler is not register");
                return;
            }
        }
        //batch callback
        if (batchCallbackHandler != null) {
            if (isFailover) {
                if (log.isDebugEnabled()) {
                    log.debug("[callbackRS]start fail-over batch rs height:{}", blockHeader.getHeight());
                }
                batchCallbackHandler.onFailover(txs, txReceipts, blockHeader);
                return;
            }
            //callback business
            if (log.isDebugEnabled()) {
                log.info("[callbackRS]start batchCallback rs height:{}", blockHeader.getHeight());
            }
            if (isClusterPersisted) {
                batchCallbackHandler.onClusterPersisted(txs, txReceipts, blockHeader);
            } else {
                batchCallbackHandler.onPersisted(txs, txReceipts, blockHeader);
            }
            if (log.isDebugEnabled()) {
                log.info("[callbackRS]end batchCallback rs height:{}", blockHeader.getHeight());
            }
            return;
        }
        //for each callback
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
            if (isFailover) {
                if (log.isDebugEnabled()) {
                    log.debug("[callbackRS]start fail over rs txId:{}", txId);
                }
                callbackHandler.onFailover(respData, tx.getSignatureList(), blockHeader);
                continue;
            }
            //callback business
            if (log.isDebugEnabled()) {
                log.info("[callbackRS]start callback rs txId:{}", txId);
            }
            if (isClusterPersisted) {
                callbackHandler.onClusterPersisted(respData, tx.getSignatureList(), blockHeader);
            } else {
                callbackHandler.onPersisted(respData, tx.getSignatureList(), blockHeader);
            }
            if (log.isDebugEnabled()) {
                log.info("[callbackRS]end callback rs txId:{}", txId);
            }
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
