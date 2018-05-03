package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.transaction.TransactionExecutor;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
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

    /**
     * execute package persisting, get persist result and submit consensus layer
     *
     * @param packageData
     */
    public void persisting(PackageData packageData) {
        log.info("[PackagePersistor.persisting] is start");
        Package pack = packageData.getCurrentPackage();
        List<SignedTransaction> txs = pack.getSignedTxList();
        if (CollectionUtils.isEmpty(txs)) {
            log.error("[package.persisting]the transactions in the package is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TXS_IS_EMPTY_ERROR);
        }
        //snapshot transactions should be init
        snapshotService.destroy();
        try {
            //gets the block hash from db
            BlockHeader tempHeader = blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.TEMP_TYPE);
            //check hash
            if (tempHeader == null) {
                log.error("[package.persisting] temp hash of db is null");
                throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
            }
            //persist all transactions
            List<TransactionReceipt> txReceipts = executeTransactions(packageData);
            //build a new block hash from db datas
            BlockHeader dbHeader = blockService.buildHeader(TxProcessTypeEnum.PERSIST, packageData, txReceipts);
            //check dbHeader and tempHeader
            if (!StringUtils.equals(dbHeader.getBlockHash(), tempHeader.getBlockHash())) {
                log.error("[package.persisting] blockHeader.hash is unequals");
                throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_UNEQUAL_ERROR);
            }
            //build block and save to context
            Block block = blockService.buildBlock(packageData, dbHeader);
            packageData.setCurrentBlock(block);

            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                    //persist block
                    blockService.persistBlock(block,txReceipts);
                }
            });
        } catch (Throwable e) {
            log.error("[package.persisting]has unknown error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_PERSISTING_ERROR, e);
        } finally {
            //snapshot transactions should be destroy
            snapshotService.destroy();
        }
        log.info("[PackagePersistor.persisting] is end");
    }

    /**
     * loop validate all transactions
     *
     * @param packageData
     * @return
     */
    private List<TransactionReceipt> executeTransactions(PackageData packageData) {
        log.info("[PackagePersistor.executeTransactions] is end");
        List<SignedTransaction> txs = packageData.getCurrentPackage().getSignedTxList();
        List<String> dbTxs = transactionRepository.queryTxIds(txs);
        List<SignedTransaction> persistedDatas = new ArrayList<>();
        List<TransactionReceipt> txReceipts = new ArrayList<>(txs.size());
        //loop validate each transaction
        for (SignedTransaction tx : txs) {
            //ignore idempotent transaction
            if(hasTx(dbTxs,tx.getCoreTx().getTxId())){
                continue;
            }
            //set current transaction and execute
            packageData.setCurrentTransaction(tx);
            TransactionReceipt receipt = transactionExecutor.persist(packageData.parseTransactionData());
            persistedDatas.add(tx);
            txReceipts.add(receipt);
        }
        packageData.getCurrentBlock().setSignedTxList(persistedDatas);
        log.info("[PackagePersistor.executeTransactions] is end");
        return txReceipts;
    }
    /**
     * check tx is exist by txId
     *
     * @param txs
     * @param txId
     * @return
     */
    private boolean hasTx(List<String> txs,String txId){
        if(CollectionUtils.isEmpty(txs)){
            return false;
        }
        for(String mTxId : txs){
            if(StringUtils.equals(txId,mTxId)){
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
        log.info("[PackagePersistor.persisted] is start");
        //gets the block header from db
        BlockHeader consensHeader =
            blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.CONSENSUS_PERSIST_TYPE);
        //check hash
        if (consensHeader == null) {
            log.warn("[package.persisted] consensus header of db is null blockHeight:{}", pack.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
        }
        BlockHeader tempHeader = blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.TEMP_TYPE);
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

        //TODO:call RS business
        log.info("[PackagePersistor.persisted] is end");
    }
}

