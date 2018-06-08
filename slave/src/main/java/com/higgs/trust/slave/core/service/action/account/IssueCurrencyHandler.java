package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountSnapshotHandler;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @description issue currency
 * @date 2018-04-19
 */
@Slf4j @Component public class IssueCurrencyHandler implements ActionHandler {
    @Autowired AccountSnapshotHandler accountSnapshotHandler;

    @Override public void process(ActionData actionData) {
        log.info("[issueCurrency.process] is start");
        IssueCurrency bo = (IssueCurrency)actionData.getCurrentAction();
        if (bo == null) {
            log.error("[issueCurrency.process] convert action to IssueCurrency is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        // validate param
        BeanValidateResult validateResult = BeanValidator.validate(bo);
        if (!validateResult.isSuccess()) {
            log.error("[issueCurrency.process] param validate is fail,first msg:{}", validateResult.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
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
