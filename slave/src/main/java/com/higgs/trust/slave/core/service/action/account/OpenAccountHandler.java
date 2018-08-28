package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountSnapshotHandler;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @description open account datahandler
 * @date 2018-03-27
 */
@Slf4j @Component public class OpenAccountHandler implements ActionHandler {
    @Autowired AccountSnapshotHandler accountSnapshotHandler;

    @Override public void verifyParams(Action action) throws SlaveException {
        OpenAccount bo = (OpenAccount)action;
        if(StringUtils.isEmpty(bo.getAccountNo()) || bo.getAccountNo().length() > 64){
            log.error("[verifyParams] accountNo is null or illegal param:{}",bo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(StringUtils.isEmpty(bo.getChainOwner()) || bo.getChainOwner().length() > 24){
            log.error("[verifyParams] chainOwner is null or illegal param:{}",bo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(StringUtils.isEmpty(bo.getCurrency()) || bo.getCurrency().length() > 24){
            log.error("[verifyParams] currency is null or illegal param:{}",bo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(StringUtils.isEmpty(bo.getDataOwner()) || bo.getDataOwner().length() > 24){
            log.error("[verifyParams] dataOwner is null or illegal param:{}",bo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(bo.getFundDirection() == null){
            log.error("[verifyParams] Funddirection is null or illegal param:{}",bo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }

    @Override public void process(ActionData actionData){
        log.debug("[openAccount.process] is start ");
        OpenAccount bo = (OpenAccount)actionData.getCurrentAction();
        Profiler.enter("[validateForOpenAccount]");
        // validate business
        // check accountNo
        AccountInfo accountInfo = accountSnapshotHandler.getAccountInfo(bo.getAccountNo());
        if (accountInfo != null) {
            log.error("[openAccount.process]account is already exists for accountNo:{}", bo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_ALREADY_EXISTS_ERROR);
        }
        // check currency
        CurrencyInfo currencyInfo = accountSnapshotHandler.queryCurrency(bo.getCurrency());
        if (currencyInfo == null) {
            log.error("[openAccount.process] currency:{} is not exists", bo.getCurrency());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_CURRENCY_NOT_EXISTS_ERROR);
        }
        Profiler.release();
        //process business
        Profiler.enter("[persistForOpenAccount]");
        accountSnapshotHandler.openAccount(bo);
        Profiler.release();
        log.debug("[openAccount.process] is success");
    }
}
