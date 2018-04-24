package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountDBHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountSnapshotHandler;
import com.higgs.trust.slave.model.bo.account.AccountFreezeRecord;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.account.AccountUnFreeze;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author liuyu
 * @description unfreeze account balance
 * @date 2018-03-29
 */
@Slf4j @Component public class AccountUnFreezeHandler implements ActionHandler {
    @Autowired AccountSnapshotHandler accountSnapshotHandler;
    @Autowired AccountDBHandler accountDBHandler;

    @Override public void validate(ActionData actionData) {
        log.info("[accountUnFreeze.validate] is start");
        process(actionData, TxProcessTypeEnum.VALIDATE);
        log.info("[accountUnFreeze.validate] is success");
    }

    @Override public void persist(ActionData actionData) {
        log.info("[accountUnFreeze.persist] is start");
        process(actionData, TxProcessTypeEnum.PERSIST);
        log.info("[accountUnFreeze.persist] is success");
    }

    private void process(ActionData actionData, TxProcessTypeEnum processTypeEnum) {
        AccountUnFreeze bo = (AccountUnFreeze)actionData.getCurrentAction();
        if (bo == null) {
            log.error("[accountUnFreeze.validate] convert action to accountUnFreezeBO is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        //
        unFreeze(bo,actionData.getCurrentBlock().getBlockHeader().getHeight(),processTypeEnum);
    }

    public void unFreeze(AccountUnFreeze bo,Long blockHeight,TxProcessTypeEnum processTypeEnum){
        //validate param
        BeanValidateResult validateResult = BeanValidator.validate(bo);
        if (!validateResult.isSuccess()) {
            log.error("[accountUnFreeze.validate] param validate is fail,first msg:{}", validateResult.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        AccountHandler accountHandler = null;
        if (processTypeEnum == TxProcessTypeEnum.VALIDATE) {
            accountHandler = accountSnapshotHandler;
        } else if (processTypeEnum == TxProcessTypeEnum.PERSIST) {
            accountHandler = accountDBHandler;
        }
        //validate business
        //check record is exists
        AccountFreezeRecord freezeRecord = accountHandler.getAccountFreezeRecord(bo.getBizFlowNo(), bo.getAccountNo());
        if (freezeRecord == null) {
            log.error("[accountUnFreeze.validate] freezeRecord is not exists flowNo:{},accountNo:{}", bo.getBizFlowNo(),
                bo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_FREEZE_RECORD_IS_NOT_EXISTS_ERROR);
        }
        BigDecimal happenAmount = bo.getAmount();
        //check amount
        if (happenAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("[accountUnFreeze.validate] amount is check fail by amount:{}", happenAmount);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_UNFREEZE_AMOUNT_ERROR);
        }
        //check can unfreeze amount
        BigDecimal afterAmount = freezeRecord.getAmount().subtract(happenAmount);
        if (afterAmount.compareTo(BigDecimal.ZERO) < 0) {
            log.error("[accountUnFreeze.validate] can unfreeze amount is not enough by accountNo:{}",
                bo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_BALANCE_IS_NOT_ENOUGH_ERROR);
        }
        //get by accountNo
        AccountInfo accountInfo = accountHandler.getAccountInfo(bo.getAccountNo());
        if (accountInfo == null) {
            log.error("[accountUnFreeze.validate] account info is not exists by accountNo:{}", bo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
        }
        //check freeze amount of account info
        BigDecimal afterOfAccount = accountInfo.getFreezeAmount().subtract(happenAmount);
        if (afterOfAccount.compareTo(BigDecimal.ZERO) < 0) {
            log.error("[accountUnFreeze.validate] can unfreeze amount is not enough by accountNo:{}",
                bo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_BALANCE_IS_NOT_ENOUGH_ERROR);
        }
        //check contract address
        if (!StringUtils.isEmpty(freezeRecord.getContractAddr())) {
            //TODO:contract repository check contract
        }
        log.info("[accountUnFreeze.validate] before-freeze-record:{}", freezeRecord.getAmount());
        log.info("[accountUnFreeze.validate] after-freeze-record:{}", afterAmount);
        log.info("[accountUnFreeze.validate] before-freeze-account:{}", accountInfo.getFreezeAmount());
        log.info("[accountUnFreeze.validate] after-freeze-account:{}", afterOfAccount);
        //unfreeze
        accountHandler.unfreeze(bo, freezeRecord, blockHeight);
    }
}
