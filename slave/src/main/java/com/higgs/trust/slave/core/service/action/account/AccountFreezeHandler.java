package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.action.contract.AccountContractBindingHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountDBHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountSnapshotHandler;
import com.higgs.trust.slave.model.bo.Contract;
import com.higgs.trust.slave.model.bo.account.AccountFreeze;
import com.higgs.trust.slave.model.bo.account.AccountFreezeRecord;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import com.higgs.trust.slave.model.bo.contract.AccountContractBindingAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author liuyu
 * @description freeze account balance
 * @date 2018-03-29
 */
@Slf4j @Component public class AccountFreezeHandler implements ActionHandler {
    @Autowired AccountSnapshotHandler accountSnapshotHandler;
    @Autowired AccountDBHandler accountDBHandler;
    @Autowired AccountContractBindingHandler accountContractBindingHandler;
    @Autowired ContractRepository contractRepository;

    @Override public void validate(ActionData actionData) {
        log.info("[accountFreeze.validate] is start");
        process(actionData, TxProcessTypeEnum.VALIDATE);
        log.info("[accountFreeze.validate] is success");
    }

    @Override public void persist(ActionData actionData) {
        log.info("[accountFreeze.persist] is start");
        process(actionData, TxProcessTypeEnum.PERSIST);
        log.info("[accountFreeze.persist] is success");
    }

    private void process(ActionData actionData, TxProcessTypeEnum processTypeEnum) {
        AccountFreeze bo = (AccountFreeze)actionData.getCurrentAction();
        if (bo == null) {
            log.error("[accountFreeze.validate] convert action to AccountFreeze is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        //validate param
        BeanValidateResult validateResult = BeanValidator.validate(bo);
        if (!validateResult.isSuccess()) {
            log.error("[accountFreeze.validate] param validate is fail,first msg:{}", validateResult.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        AccountHandler accountHandler = null;
        if (processTypeEnum == TxProcessTypeEnum.VALIDATE) {
            accountHandler = accountSnapshotHandler;
        } else if (processTypeEnum == TxProcessTypeEnum.PERSIST) {
            accountHandler = accountDBHandler;
        }
        //validate business
        AccountInfo accountInfo = accountHandler.getAccountInfo(bo.getAccountNo());
        if (accountInfo == null) {
            log.error("[accountFreeze.validate] account info is not exists by accountNo:{}", bo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
        }
        //check amount
        if (bo.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("[accountFreeze.validate] amount is check fail by amount:{}", bo.getAmount());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_FREEZE_AMOUNT_ERROR);
        }
        //check balance
        BigDecimal afterAmount =
            accountInfo.getBalance().subtract(accountInfo.getFreezeAmount()).subtract(bo.getAmount());
        if (afterAmount.compareTo(BigDecimal.ZERO) < 0) {
            log.error("[accountFreeze.validate] balance is not enough by accountNo:{}", bo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_BALANCE_IS_NOT_ENOUGH_ERROR);
        }
        //bind contract address
        String contractBindHash = bindContract(actionData, bo, processTypeEnum);
        bo.setContractAddr(contractBindHash);

        //check record is already exists
        AccountFreezeRecord freezeRecord = accountHandler.getAccountFreezeRecord(bo.getBizFlowNo(), bo.getAccountNo());
        if (freezeRecord != null) {
            log.error("[accountFreeze.validate] freezeRecord is already exists flowNo:{},accountNo:{}",
                bo.getBizFlowNo(), bo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        //freeze
        accountHandler.freeze(bo, actionData.getCurrentBlock().getBlockHeader().getHeight());
    }

    /**
     * bind contract address
     *
     * @param accountFreeze
     * @param processTypeEnum
     */
    private String bindContract(ActionData actionData, AccountFreeze accountFreeze, TxProcessTypeEnum processTypeEnum) {
        if (StringUtils.isEmpty(accountFreeze.getContractAddr())) {
            return null;
        }
        Contract contract = contractRepository.queryByAddress(accountFreeze.getContractAddr());
        if (contract == null) {
            log.error("[accountFreeze.checkContract] contractAddr is not exist");
            throw new SlaveException(SlaveErrorEnum.SLAVE_CONTRACT_NOT_EXIST_ERROR);
        }
        AccountContractBindingAction action = new AccountContractBindingAction();
        action.setType(ActionTypeEnum.BIND_CONTRACT);
        action.setIndex(accountFreeze.getIndex());
        action.setAccountNo(accountFreeze.getAccountNo());
        action.setContractAddress(contract.getAddress());
        action.setArgs(accountFreeze.getContractArgs());
        long blockHeight = actionData.getCurrentBlock().getBlockHeader().getHeight();
        String txId = actionData.getCurrentTransaction().getCoreTx().getTxId();
        log.info("[accountFreeze.bindContract] blockHeight:{},txId:{}", blockHeight, txId);
        log.info("[accountFreeze.bindContract] contractAddr:{}", contract.getAddress());
        log.info("[accountFreeze.bindContract] contractArgs:{}", accountFreeze.getContractArgs());
        //bind contract
        AccountContractBinding accountContractBinding =
            accountContractBindingHandler.process(action, blockHeight, txId, processTypeEnum);
        return accountContractBinding.getHash();
    }
}
