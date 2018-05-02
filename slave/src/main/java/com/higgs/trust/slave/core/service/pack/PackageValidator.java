package com.higgs.trust.slave.core.service.pack;

import cn.primeledger.stability.log.TraceMonitor;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.transaction.TransactionExecutor;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.context.PackageData;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @author: pengdi
 **/
@Slf4j @Service public class PackageValidator {
    @Autowired BlockService blockService;
    @Autowired TransactionExecutor transactionExecutor;
    @Autowired TransactionRepository transactionRepository;
    @Autowired SnapshotService snapshotService;

    /**
     * execute package validating, get validate result and submit consensus layer
     *
     * @param packageData
     */
    @TraceMonitor public void validating(PackageData packageData) {
        log.info("[PackageValidator.validating] is start");
        Package pack = packageData.getCurrentPackage();
        List<SignedTransaction> txs = pack.getSignedTxList();
        if (CollectionUtils.isEmpty(txs)) {
            log.error("[package.validating]the transactions in the package is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TXS_IS_EMPTY_ERROR);
        }
        try {
            //snapshot transactions should be init
            snapshotService.destroy();
            //validate all transactions
            List<TransactionReceipt> txReceipts = executeTransactions(packageData);
            //build a new block hash
            BlockHeader blockHeader = blockService.buildHeader(TxProcessTypeEnum.VALIDATE, packageData, txReceipts);
            //persist block hash to tmp and context
            blockService.storeTempHeader(blockHeader, BlockHeaderTypeEnum.TEMP_TYPE);
            packageData.getCurrentBlock().setBlockHeader(blockHeader);
        } catch (Throwable e) {
            log.error("[package.validating]has error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_VALIDATING_ERROR, e);
        } finally {
            //snapshot transactions should be destory
            snapshotService.destroy();
        }
        log.info("[PackageValidator.validating] is end");
    }

    /**
     * loop validate all transactions
     *
     * @param packageData
     * @return
     */
    private List<TransactionReceipt> executeTransactions(PackageData packageData) {
        log.info("[PackageValidator.executeTransactions] is start");
        List<SignedTransaction> txs = packageData.getCurrentPackage().getSignedTxList();
        List<String> dbTxs = transactionRepository.queryTxIds(txs);
        List<SignedTransaction> validatedDatas = new ArrayList<>();
        List<TransactionReceipt> txReceipts = new ArrayList<>(txs.size());
        //loop validate each transaction
        for (SignedTransaction tx : txs) {
            //ignore idempotent transaction
            if (hasTx(dbTxs, tx.getCoreTx().getTxId())) {
                continue;
            }
            packageData.setCurrentTransaction(tx);
            TransactionReceipt receipt = transactionExecutor.validate(packageData.parseTransactionData());
            validatedDatas.add(tx);
            txReceipts.add(receipt);
        }
        packageData.getCurrentBlock().setSignedTxList(validatedDatas);
        log.info("[PackageValidator.executeTransactions] is end");
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
     * receive validate final result from consensus layer
     *
     * @param pack
     */
    public void validated(Package pack) {
        /**
         * TODO
         * 1.开启事务
         * 2.通过packageRepository更新package的状态为validated
         * 3.通过pendingState通知业务RS每个交易的接收结果
         * 4.提交事务
         */
        log.info("[PackageValidator.validated] is start");
        //gets the block header from db
        BlockHeader consensHeader =
            blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.CONSENSUS_VALIDATE_TYPE);
        //check hash
        if (consensHeader == null) {
            log.warn("[package.validated] consensus header of db is null blockHeight:{}", pack.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
        }
        BlockHeader tempHeader = blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.TEMP_TYPE);
        if (tempHeader == null) {
            log.error("[package.validated] temp header of db is null blockHeight:{}", pack.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
        }
        //compare
        boolean r = blockService.compareBlockHeader(consensHeader, tempHeader);
        if (!r) {
            log.error("[package.validated] consensus header unequal tempHeader,blockHeight:{}", pack.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TWO_HEADER_UNEQUAL_ERROR);
        }

        //TODO:call RS business
        log.info("[PackageValidator.validated] is end");
    }
}
