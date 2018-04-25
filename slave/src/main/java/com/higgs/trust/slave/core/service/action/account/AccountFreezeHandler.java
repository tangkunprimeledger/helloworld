package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.repository.contract.AccountContractBindingRepository;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.contract.StandardContractContextService;
import com.higgs.trust.slave.core.service.contract.StandardExecuteContextData;
import com.higgs.trust.slave.core.service.datahandler.account.AccountDBHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountSnapshotHandler;
import com.higgs.trust.slave.core.service.snapshot.agent.AccountContractBindingSnapshotAgent;
import com.higgs.trust.slave.model.bo.Contract;
import com.higgs.trust.slave.model.bo.account.AccountFreeze;
import com.higgs.trust.slave.model.bo.account.AccountFreezeRecord;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liuyu
 * @description freeze account balance
 * @date 2018-03-29
 */
@Slf4j @Component public class AccountFreezeHandler implements ActionHandler {
    @Autowired AccountSnapshotHandler accountSnapshotHandler;
    @Autowired AccountDBHandler accountDBHandler;
    @Autowired AccountContractBindingSnapshotAgent accountContractBindingSnapshotAgent;
    @Autowired AccountContractBindingRepository accountContractBindingRepository;
    @Autowired ContractRepository contractRepository;


    @Override public void validate(ActionData actionData) {
        log.info("[accountFreeze.validate] is start");
       process(actionData,TxProcessTypeEnum.VALIDATE);
        log.info("[accountFreeze.validate] is success");
    }

    @Override public void persist(ActionData actionData) {
        log.info("[accountFreeze.persist] is start");
        process(actionData,TxProcessTypeEnum.PERSIST);
        log.info("[accountFreeze.persist] is success");
    }

    private void process(ActionData actionData,TxProcessTypeEnum processTypeEnum){
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
        if(processTypeEnum == TxProcessTypeEnum.VALIDATE){
            accountHandler = accountSnapshotHandler;
        }else if(processTypeEnum == TxProcessTypeEnum.PERSIST){
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
        //check contract address
        checkContract(bo.getContractAddr(),bo.getAccountNo(),processTypeEnum);
        //check record is already exists
        AccountFreezeRecord freezeRecord =
            accountHandler.getAccountFreezeRecord(bo.getBizFlowNo(), bo.getAccountNo());
        if (freezeRecord != null) {
            log.error("[accountFreeze.validate] freezeRecord is already exists flowNo:{},accountNo:{}",
                bo.getBizFlowNo(), bo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        //freeze
        accountHandler.freeze(bo,actionData.getCurrentBlock().getBlockHeader().getHeight());
    }

    /**
     * check contract address
     *
     * @param contractAddr
     * @param accountNo
     * @param processTypeEnum
     */
    private void checkContract(String contractAddr,String accountNo,TxProcessTypeEnum processTypeEnum){
        if (StringUtils.isEmpty(contractAddr)) {
            return;
        }
        Contract contract = contractRepository.queryByAddress(contractAddr);
        if(contract == null){
            log.error("[accountFreeze.checkContract] contractAddr is not exist");
            throw new SlaveException(SlaveErrorEnum.SLAVE_CONTRACT_NOT_EXIST_ERROR);
        }
        //TODO:liuyu 冻结时的合约 约定规范1.冻结时绑定2.绑定后冻结3.冻结后绑定
        List<AccountContractBinding> bindingList = null;
        if (processTypeEnum == TxProcessTypeEnum.VALIDATE) {
            bindingList = accountContractBindingSnapshotAgent.get(accountNo);
        } else if (processTypeEnum == TxProcessTypeEnum.PERSIST) {
            bindingList = accountContractBindingRepository.queryListByAccountNo(accountNo);
        }
        if (CollectionUtils.isEmpty(bindingList)) {
            log.error("[accountFreeze.checkContract] bindingList is not exist");
            throw new SlaveException(SlaveErrorEnum.SLAVE_CONTRACT_NOT_EXIST_ERROR);
        }
        boolean hasAddr = false;
        for (AccountContractBinding binding : bindingList) {
            if(StringUtils.equals(binding.getContractAddress(),contractAddr)){
                hasAddr = true;
                break;
            }
        }
        if(!hasAddr){
            log.error("[accountFreeze.checkContract] contractAddr is not exist");
            throw new SlaveException(SlaveErrorEnum.SLAVE_CONTRACT_NOT_EXIST_ERROR);
        }
    }
}
