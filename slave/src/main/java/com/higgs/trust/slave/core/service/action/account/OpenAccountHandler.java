package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountDBHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountSnapshotHandler;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @description open account datahandler
 * @date 2018-03-27
 */
@Slf4j @Component public class OpenAccountHandler implements ActionHandler {
    @Autowired AccountSnapshotHandler accountSnapshotHandler;
    @Autowired AccountDBHandler accountDBHandler;

    @Override public void validate(ActionData actionData) {
        process(actionData,TxProcessTypeEnum.VALIDATE);
    }

    @Override public void persist(ActionData actionData) {
        process(actionData,TxProcessTypeEnum.PERSIST);
    }

    private void process(ActionData actionData,TxProcessTypeEnum processTypeEnum){
        log.info("[openAccount.process] is start prosessType:{}",processTypeEnum);
        OpenAccount bo = (OpenAccount)actionData.getCurrentAction();
        if (bo == null) {
            log.error("[openAccount.process] convert action to OpenAccountBo is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        // validate param
        BeanValidateResult validateResult = BeanValidator.validate(bo);
        if (!validateResult.isSuccess()) {
            log.error("[openAccount.process] param validate is fail,first msg:{}", validateResult.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        AccountHandler accountHandler = null;
        if(processTypeEnum == TxProcessTypeEnum.VALIDATE){
            accountHandler = accountSnapshotHandler;
        }else if(processTypeEnum == TxProcessTypeEnum.PERSIST){
            accountHandler = accountDBHandler;
        }
        Profiler.enter("[validateForOpenAccount]");
        // validate business
        // check accountNo
        AccountInfo accountInfo = accountHandler.getAccountInfo(bo.getAccountNo());
        if (accountInfo != null) {
            log.error("[openAccount.process] is idempotent for accountNo:{}", bo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        // check currency
        CurrencyInfo currencyInfo = accountHandler.queryCurrency(bo.getCurrency());
        if (currencyInfo == null) {
            log.error("[openAccount.process] currency:{} is not exists", bo.getCurrency());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_CURRENCY_NOT_EXISTS_ERROR);
        }
        Profiler.release();
        //process business
        Profiler.enter("[persistForOpenAccount]");
        accountHandler.openAccount(bo);
        Profiler.release();
        log.info("[openAccount.process] is success");
    }
}
