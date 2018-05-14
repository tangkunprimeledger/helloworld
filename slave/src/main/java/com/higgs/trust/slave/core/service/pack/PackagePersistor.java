package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.api.SlaveCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackRegistor;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.context.AppContext;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.transaction.TransactionExecutor;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.context.PackageData;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @author: pengdi
 **/
@Slf4j @Service public class PackagePersistor {
    @Autowired TransactionTemplate txRequired;
    @Autowired BlockService blockService;
    @Autowired TransactionExecutor transactionExecutor;
    @Autowired SnapshotService snapshotService;
    @Autowired TransactionRepository transactionRepository;
    @Autowired SlaveCallbackRegistor slaveCallbackRegistor;

    /**
     * execute package persisting, get persist result and submit consensus layer
     *
     * @param packageData
     */
    public void persisting(PackageData packageData) {
        Profiler.start("[PackageValidator.persisting.monitor]");
        Package pack = packageData.getCurrentPackage();
        List<SignedTransaction> txs = pack.getSignedTxList();
        if (CollectionUtils.isEmpty(txs)) {
            log.error("[package.persisting]the transactions in the package is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TXS_IS_EMPTY_ERROR);
        }
        //snapshot transactions should be init
        snapshotService.destroy();
        try {
            Profiler.enter("[query temp header of TEMP_TYPE]");
            //gets the block hash from db
            BlockHeader tempHeader = blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.TEMP_TYPE);
            //check hash
            if (tempHeader == null) {
                log.error("[package.persisting] temp hash of db is null");
                throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
            }
            Profiler.release();
            Profiler.enter("[execute txs]");
            //persist all transactions
            List<TransactionReceipt> txReceipts = executeTransactions(packageData);
            Profiler.release();
            Profiler.enter("[build block header]");
            //build a new block hash from db datas
            BlockHeader dbHeader = blockService.buildHeader(TxProcessTypeEnum.PERSIST, packageData, txReceipts);
            Profiler.release();
            //check dbHeader and tempHeader
            if (!StringUtils.equals(dbHeader.getBlockHash(), tempHeader.getBlockHash())) {
                log.error("[package.persisting] blockHeader.hash is unequals");
                throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_UNEQUAL_ERROR);
            }
            Profiler.enter("[persist block]");
            //build block and save to context
            Block block = blockService.buildBlock(packageData, dbHeader);
            packageData.setCurrentBlock(block);
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
        } catch (Throwable e) {
            log.error("[package.persisting]has unknown error");
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
                TransactionReceipt receipt = transactionExecutor.persist(packageData.parseTransactionData());
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
     * @param pack
     */
    public void persisted(Package pack) {
        /**
         * TODO
         * 1.开启事务
         * 2.通过packageRepository更新package的状态为persisted
         * 3.通过pendingState触发业务RS的callback操作
         * 4.提交事务
         */
        Profiler.start("[PackagePersistor.persisted] is start");
        Profiler.enter("[start query temp header of CONSENSUS_VALIDATE_TYPE]");
        //gets the block header from db
        BlockHeader consensHeader =
            blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.CONSENSUS_PERSIST_TYPE);
        Profiler.release();
        //check hash
        if (consensHeader == null) {
            log.warn("[package.persisted] consensus header of db is null blockHeight:{}", pack.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
        }
        Profiler.enter("[start query temp header of TEMP_TYPE]");
        BlockHeader tempHeader = blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.TEMP_TYPE);
        Profiler.release();
        if (tempHeader == null) {
            log.error("[package.persisted] temp header of db is null blockHeight:{}", pack.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
        }
        //compare
        boolean r = blockService.compareBlockHeader(consensHeader, tempHeader);
        if (!r) {
            log.error("[package.persisted] consensus header unequal tempHeader,blockHeight:{}", pack.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TWO_HEADER_UNEQUAL_ERROR);
        }
        try {
            //call back business
            Profiler.enter("[callbackRSForClusterPersisted]");
            List<SignedTransaction> txs = transactionRepository.queryTransactions(pack.getHeight());
            List<TransactionReceipt> txReceipts = transactionRepository.queryTxReceipts(txs);
            callbackRS(txs, txReceipts,true);
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
                callbackHandler.onClusterPersisted(respData);
            }else{
                callbackHandler.onPersisted(respData);
            }
            log.info("[callbackRS]end callback rs txId:{}", txId);
        }
    }

}

