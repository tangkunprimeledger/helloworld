package com.higgs.trust.slave.core.service.transaction;

import com.higgs.trust.contract.SmartContractException;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.MerkleException;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.exception.SnapshotException;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author WangQuanzhou
 * @desc the class that handle SignedTransaction and CoreTransaction
 * @date 2018/3/27 14:54
 */
@Slf4j @Component public class TransactionExecutorImpl implements TransactionExecutor {
    @Autowired TransactionTemplate txNested;
    @Autowired TxProcessorHolder processorHolder;
    @Autowired TxCheckHandler txCheckHandler;
    @Autowired SnapshotService snapshot;

    @Override public TransactionReceipt validate(TransactionData transactionData) {
        log.info("[TransactionExecutor.validate] is start");
        SignedTransaction tx = transactionData.getCurrentTransaction();
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTxId(tx.getCoreTx().getTxId());
        try {
            //snapshot transactions should be started
            snapshot.startTransaction();
            //execute validate
            execute(transactionData, TxProcessTypeEnum.VALIDATE);
            //snapshot transactions should be commit
            snapshot.commit();
            receipt.setResult(true);
        } catch (SnapshotException e) {
            log.error("[validate]has SnapshotException");
            //should retry package process
            throw e;
        } catch (SmartContractException e) {
            log.error("[validate]has SmartContractException");
            //should retry package process
            throw e;
        } catch (MerkleException e) {
            log.error("[validate]has MerkleException");
            //should retry package process
            throw e;
        } catch (SlaveException e) {
            log.error("[validate]has error", e);
            //snapshot transactions should be rollback
            snapshot.rollback();
            receipt.setErrorCode(e.getCode().getCode());
        } catch (Throwable e) {
            log.error("[validate]has error", e);
            //snapshot transactions should be rollback
            snapshot.rollback();
            receipt.setErrorCode(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION.getCode());
        }

        log.info("[TransactionExecutor.validate] is end");
        return receipt;
    }

    @Override public TransactionReceipt persist(TransactionData transactionData) {
        log.info("[persist]is start");
        SignedTransaction tx = transactionData.getCurrentTransaction();

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTxId(tx.getCoreTx().getTxId());
        try {
            //execute persist
            execute(transactionData, TxProcessTypeEnum.PERSIST);
            receipt.setResult(true);
        } catch (SmartContractException e) {
            log.error("[persist]has SmartContractException");
            //should retry package process
            throw e;
        } catch (MerkleException e) {
            log.error("[persist]has MerkleException");
            //should retry package process
            throw e;
        } catch (SlaveException e) {
            log.error("[persist]has error", e);
            receipt.setErrorCode(e.getCode().getCode());
        } catch (Throwable e) {
            log.error("[persist]has error", e);
            receipt.setErrorCode(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION.getCode());
        }

        log.info("[persist]is end");
        return receipt;
    }

    private void execute(TransactionData transactionData, TxProcessTypeEnum processTypeEnum) {
        SignedTransaction signedTransaction = transactionData.getCurrentTransaction();

        //param validation
        if (null == signedTransaction || null == signedTransaction.getCoreTx()) {
            log.error("SignedTransaction is invalid, ", signedTransaction);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        //verify signatures
        if (!txCheckHandler.verifySignatures(signedTransaction)) {
            log.error("SignedTransaction verify signature failed");
            throw new SlaveException(SlaveErrorEnum.SLAVE_TX_VERIFY_SIGNATURE_FAILED);
        }

        //  start to handle CoreTransaction, first step, get CoreTransaction from SignedTransaction
        CoreTransaction coreTx = signedTransaction.getCoreTx();

        // check action, if action type equals REGISTER_POLICY or REGISTER_RS, current transaction can have only one action.
        if (!txCheckHandler.checkActions(coreTx)) {
            log.error("core transaction is invalid, ", coreTx);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // acquire version information
        String version = coreTx.getVersion();
        // get exact handler based on version
        TransactionProcessor processor = processorHolder.getProcessor(VersionEnum.getBizTypeEnumBycode(version));
        //ensure that all actions are transactional
        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                processor.process(transactionData, processTypeEnum);
            }
        });
    }
}
