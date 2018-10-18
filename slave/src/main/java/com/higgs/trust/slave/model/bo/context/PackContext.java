package com.higgs.trust.slave.model.bo.context;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @Description:
 * @author: pengdi
 **/
@Slf4j public class PackContext implements PackageData, TransactionData, ActionData, CommonData {

    /**
     * do not validate package because it's too expensive
     */
    @NotNull private Package pack;

    /**
     * validate block, it's cheap
     */
    @NotNull @Valid private Block block;

    /**
     * index to current transaction
     */
    private SignedTransaction currentTransaction;

    /**
     * index to current action
     */
    private Action currentAction;

    /**
     * rs and public key map
     */
    private Map<String, String> rsPubKeyMap;

    /**
     * package context constructor only
     *
     * @param pack
     * @param block
     */
    public PackContext(Package pack, Block block) {
        this.pack = pack;
        this.block = block;
        this.currentTransaction = null;
        this.currentAction = null;
        this.rsPubKeyMap = null;
    }

    /**
     * set current transaction
     *
     * @param transaction
     * @return
     */
    @Override public void setCurrentTransaction(SignedTransaction transaction) {
        if (transaction == null) {
            log.error("transaction is null!");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        this.currentTransaction = transaction;
    }

    /**
     * handle next transaction in this package processing
     *
     * @return
     */
    @Override public TransactionData parseTransactionData() {
        return this;
    }

    /**
     * set the package
     *
     * @param pack
     */
    @Override public void setCurrentPackage(Package pack) {
        this.pack = pack;
    }

    /**
     * set the block
     *
     * @param block
     */
    @Override public void setCurrentBlock(Block block) {
        this.block = block;
    }

    @Override public void setRsPubKeyMap(Map<String, String> rsPubKeyMap) {
        this.rsPubKeyMap = rsPubKeyMap;
    }

    @Override public Map<String, String> getRsPubKeyMap() {
        return rsPubKeyMap;
    }

    /**
     * get the block generating by this package
     *
     * @return
     */
    @Override public Block getCurrentBlock() {
        if (block == null) {
            log.error("block is null!");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        return block;
    }

    /**
     * get the package
     *
     * @return
     */
    @Override public Package getCurrentPackage() {
        if (pack == null) {
            log.error("pack is null!");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        return pack;
    }

    /**
     * get the executing transaction in this package processing
     *
     * @return
     */
    @Override public SignedTransaction getCurrentTransaction() {
        if (null == currentTransaction) {
            log.error("[PackageContext.getCurrentTransaction] context has no current transaction");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        return currentTransaction;
    }

    /**
     * get the executing action in this transaction processing
     *
     * @return
     */
    @Override public Action getCurrentAction() {
        if (null == currentAction) {
            log.error("[PackageContext.getCurrentTransaction] context has no current action");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        return currentAction;
    }

    /**
     * set the current action in this transaction processing
     *
     * @param action
     * @return
     */
    @Override public void setCurrentAction(Action action) {
        if (action == null) {
            log.error("action is null!");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        this.currentAction = action;
    }

    /**
     * @return
     */
    @Override public ActionData parseActionData() {
        return this;
    }
}
