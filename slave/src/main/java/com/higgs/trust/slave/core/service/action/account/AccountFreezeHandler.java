package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.action.contract.AccountContractBindingHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountSnapshotHandler;
import com.higgs.trust.slave.model.bo.account.AccountFreeze;
import com.higgs.trust.slave.model.bo.account.AccountFreezeRecord;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import com.higgs.trust.slave.model.bo.contract.AccountContractBindingAction;
import com.higgs.trust.slave.model.bo.contract.Contract;
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
    @Autowired AccountContractBindingHandler accountContractBindingHandler;
    @Autowired ContractRepository contractRepository;

    @Override public void process(ActionData actionData) {
        AccountFreeze bo = (AccountFreeze)actionData.getCurrentAction();
        Profiler.enter("[validateForFreeze]");
        AccountFreeze newBo = BeanConvertor.convertBean(bo, AccountFreeze.class);
        try {
            //validate business
            AccountInfo accountInfo = accountSnapshotHandler.getAccountInfo(newBo.getAccountNo());
            if (accountInfo == null) {
                log.error("[accountFreeze.validate] account info is not exists by accountNo:{}", newBo.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
            }
            //check amount
            if (bo.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("[accountFreeze.validate] amount is check fail by amount:{}", newBo.getAmount());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_FREEZE_AMOUNT_ERROR);
            }
            //check balance
            BigDecimal afterAmount =
                accountInfo.getBalance().subtract(accountInfo.getFreezeAmount()).subtract(newBo.getAmount());
            if (afterAmount.compareTo(BigDecimal.ZERO) < 0) {
                log.error("[accountFreeze.validate] balance is not enough by accountNo:{}", newBo.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_BALANCE_IS_NOT_ENOUGH_ERROR);
            }
            //bind contract address
            String contractBindHash = bindContract(actionData, newBo);
            newBo.setContractAddr(contractBindHash);
            //check record is already exists
            AccountFreezeRecord freezeRecord =
                accountSnapshotHandler.getAccountFreezeRecord(newBo.getBizFlowNo(), newBo.getAccountNo());
            if (freezeRecord != null) {
                log.error("[accountFreeze.validate] freezeRecord is already exists flowNo:{},accountNo:{}",
                    bo.getBizFlowNo(), bo.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_FREEZE_RECORD_IS_ALREADY_EXISTS_ERROR);
            }
        } finally {
            Profiler.release();
        }
        //freeze
        try {
            Profiler.enter("[persistForFreeze]");
            accountSnapshotHandler.freeze(newBo, actionData.getCurrentBlock().getBlockHeader().getHeight());
        }finally {
            Profiler.release();
        }
    }

    /**
     * bind contract address
     *
     * @param accountFreeze
     */
    private String bindContract(ActionData actionData, AccountFreeze accountFreeze) {
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
            accountContractBindingHandler.process(action, blockHeight, txId);
        return accountContractBinding.getHash();
    }
}
