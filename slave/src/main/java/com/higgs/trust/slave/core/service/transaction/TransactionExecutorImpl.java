package com.higgs.trust.slave.core.service.transaction;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.contract.SmartContractException;
import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.facade.ContractExecutionResult;
import com.higgs.trust.evmcontract.facade.exception.ContractExecutionException;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.MerkleException;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.exception.SnapshotException;
import com.higgs.trust.slave.core.Blockchain;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.version.TransactionProcessor;
import com.higgs.trust.slave.core.service.version.TxProcessorHolder;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.context.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author WangQuanzhou
 * @desc the class that handle SignedTransaction and CoreTransaction
 * @date 2018/3/27 14:54
 */
@Slf4j @Component public class TransactionExecutorImpl implements TransactionExecutor {

    @Autowired TxProcessorHolder processorHolder;
    @Autowired TxCheckHandler txCheckHandler;
    @Autowired SnapshotService snapshot;
    @Autowired InitConfig initConfig;
    @Autowired private Blockchain blockchain;

    @Override public TransactionReceipt process(TransactionData transactionData, Map<String, String> rsPubKeyMap) {
        log.debug("[TransactionExecutorImpl.persist] is start");
        SignedTransaction tx = transactionData.getCurrentTransaction();
        Repository txTrack = blockchain.getRepositorySnapshot().startTracking();

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTxId(tx.getCoreTx().getTxId());
        try {
            //snapshot transactions should be started
            snapshot.startTransaction();
            //execute persist
            execute(transactionData, rsPubKeyMap);
            //snapshot transactions should be commit
            txTrack.commit();
            snapshot.commit();
            receipt.setResult(true);
        } catch (SmartContractException e) {
            log.error("[TransactionExecutorImpl.persist] has SmartContractException", e);
            //snapshot transactions should be rollback
            txTrack.rollback();
            snapshot.rollback();
            receipt.setErrorCode(SlaveErrorEnum.SLAVE_SMART_CONTRACT_ERROR.getCode());
            receipt.setErrorMessage(e.getMessage());
        } catch (ContractExecutionException e) {
            log.error("[TransactionExecutorImpl.persist] has ContractExecutionException", e);
            txTrack.rollback();
            snapshot.rollback();
            receipt.setErrorCode(SlaveErrorEnum.SLAVE_SMART_CONTRACT_ERROR.getCode());
            receipt.setErrorMessage(e.getMessage());
        } catch (SnapshotException e) {
            log.error("[TransactionExecutorImpl.persist] has SnapshotException");
            //should retry package process
            throw e;
        } catch (MerkleException e) {
            log.error("[TransactionExecutorImpl.persist] has MerkleException");
            //should retry package process
            throw e;
        } catch (SlaveException e) {
            log.error("[TransactionExecutorImpl.persist] has error", e);
            //snapshot transactions should be rollback
            txTrack.rollback();
            snapshot.rollback();
            receipt.setErrorCode(e.getCode().getCode());
            receipt.setErrorMessage(e.getMessage());
        } catch (Throwable e) {
            log.error("[TransactionExecutorImpl.persist] has error", e);
            //snapshot transactions should be rollback
            txTrack.rollback();
            snapshot.rollback();
            receipt.setErrorCode(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION.getCode());
            receipt.setErrorMessage(e.getMessage());
        }

        log.debug("[TransactionExecutorImpl.persist] is end");
        return receipt;
    }

    private void execute(TransactionData transactionData, Map<String, String> rsPubKeyMap) {
        SignedTransaction signedTransaction = transactionData.getCurrentTransaction();
        CoreTransaction coreTx = signedTransaction.getCoreTx();
        try {
            Profiler.enter("[tx.verifySignatures]");
            //verify signatures
            if (!txCheckHandler.verifySignatures(signedTransaction, rsPubKeyMap)) {
                log.error("SignedTransaction verify signature failed, signedTransaction={}, rsPubKeyMap={}",
                    signedTransaction.toString(), rsPubKeyMap.toString());
                throw new SlaveException(SlaveErrorEnum.SLAVE_TX_VERIFY_SIGNATURE_FAILED);
            }
            // check action, if action type equals REGISTER_POLICY or REGISTER_RS, current transaction can have only one action.
            if (!txCheckHandler.checkActions(coreTx)) {
                log.error("core transaction is invalid, txId={}", coreTx.getTxId());
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
        } finally {
            Profiler.release();
        }
        // acquire version information
        String version = coreTx.getVersion();
        // get exact handler based on version
        TransactionProcessor processor = processorHolder.getProcessor(VersionEnum.getBizTypeEnumBycode(version));
        //process tx
        processor.process(transactionData);
    }
}
