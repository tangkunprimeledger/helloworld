package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountSnapshotHandler;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @description issue currency
 * @date 2018-04-19
 */
@Slf4j @Component public class IssueCurrencyHandler implements ActionHandler {
    @Autowired AccountSnapshotHandler accountSnapshotHandler;

    @Override public void verifyParams(Action action) throws SlaveException {
        IssueCurrency bo = (IssueCurrency)action;
        if(StringUtils.isEmpty(bo.getCurrencyName()) || bo.getCurrencyName().length() > 24){
            log.error("[verifyParams] currencyName is null or illegal param:{}",bo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }

    @Override public void process(ActionData actionData) {
        log.info("[issueCurrency.process] is start");
        IssueCurrency bo = (IssueCurrency)actionData.getCurrentAction();
        Profiler.enter("[validateForIssueCurrency]");
        // check currency
        CurrencyInfo currencyInfo = accountSnapshotHandler.queryCurrency(bo.getCurrencyName());
        if (currencyInfo != null) {
            log.error("[issueCurrency.process] currency:{} is already exists", bo.getCurrencyName());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_CURRENCY_ALREADY_EXISTS_ERROR);
        }
        Profiler.release();
        //process business
        Profiler.enter("[persistForIssueCurrency]");
        accountSnapshotHandler.issueCurrency(bo);
        Profiler.release();
        log.info("[issueCurrency.process] is success");
    }
}
