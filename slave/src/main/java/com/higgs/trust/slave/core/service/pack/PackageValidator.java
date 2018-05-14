package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.api.SlaveCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackRegistor;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
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
    @Autowired SlaveCallbackRegistor slaveCallbackRegistor;

    /**
     * execute package validating, get validate result and submit consensus layer
     *
     * @param packageData
     */
    public void validating(PackageData packageData) {
        Profiler.start("[PackageValidator.validating.monitor]");

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
            Profiler.enter("[execute txs]");
            List<TransactionReceipt> txReceipts = executeTransactions(packageData);
            Profiler.release();
            //build a new block hash
            Profiler.enter("[build block header]");
            BlockHeader blockHeader = blockService.buildHeader(TxProcessTypeEnum.VALIDATE, packageData, txReceipts);
            Profiler.release();
            //persist block hash to tmp and context
            Profiler.enter("[store block header]");
            blockService.storeTempHeader(blockHeader, BlockHeaderTypeEnum.TEMP_TYPE);
            Profiler.release();
            packageData.getCurrentBlock().setBlockHeader(blockHeader);
        } catch (Throwable e) {
            log.error("[package.validating]has error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_VALIDATING_ERROR, e);
        } finally {
            //snapshot transactions should be destory
            snapshotService.destroy();
            Profiler.release();
            Profiler.logDump();
        }
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

        List<SignedTransaction> validatedDatas = new ArrayList<>();
        List<TransactionReceipt> txReceipts = new ArrayList<>(txs.size());
        //loop validate each transaction
        for (SignedTransaction tx : txs) {
            String title = new StringBuffer("[execute tx ").append(tx.getCoreTx().getTxId()).append("]").toString();
            Profiler.enter(title);
            //ignore idempotent transaction
            if (hasTx(dbTxs, tx.getCoreTx().getTxId())) {
                continue;
            }
            packageData.setCurrentTransaction(tx);
            TransactionReceipt receipt = transactionExecutor.validate(packageData.parseTransactionData());
            validatedDatas.add(tx);
            txReceipts.add(receipt);
            Profiler.release();
        }
        packageData.getCurrentBlock().setSignedTxList(validatedDatas);
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
        Profiler.start("PackageValidator.validated] is start");
        //gets the block header from db
        Profiler.enter("[query temp header of CONSENSUS_VALIDATE_TYPE]");
        BlockHeader consensHeader =
            blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.CONSENSUS_VALIDATE_TYPE);
        Profiler.release();
        //check hash
        if (consensHeader == null) {
            log.warn("[package.validated] consensus header of db is null blockHeight:{}", pack.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
        }
        Profiler.enter("[query temp header of TEMP_TYPE]");
        BlockHeader tempHeader = blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.TEMP_TYPE);
        Profiler.release();
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
        //call RS business
        try {
            Profiler.enter("[callbackRSForValidate]");
            callbackRS(pack);
            Profiler.release();
        }catch (Throwable e){
            log.error("[package.validated] callback rs has error",e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_CALLBACK_ERROR);
        }finally {
            //profiler log
            Profiler.release();
            if (Profiler.getDuration() > 0) {
                log.info(Profiler.dump());
            }
        }
    }

    /**
     * call back business
     */
    private void callbackRS(Package pack){
        SlaveCallbackHandler callbackHandler = slaveCallbackRegistor.getSlaveCallbackHandler();
        if (callbackHandler == null) {
            log.error("[callbackRS]callbackHandler is not register");
            throw new SlaveException(SlaveErrorEnum.SLAVE_RS_CALLBACK_NOT_REGISTER_ERROR);
        }
        List<SignedTransaction> txs = pack.getSignedTxList();
        if (CollectionUtils.isEmpty(txs)) {
            log.error("[callbackRS]txs is empty from pack:{}", pack);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_TXS_IS_EMPTY_ERROR);
        }
        for (SignedTransaction tx : txs) {
            //call back business
            callbackHandler.onValidated(tx.getCoreTx().getTxId());
        }
    }
}
