package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountDBHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountSnapshotHandler;
import com.higgs.trust.slave.model.bo.account.AccountOperation;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @description account operation datahandler
 * @date 2018-03-28
 */
@Slf4j @Component public class AccountOperationHandler implements ActionHandler {
    @Autowired AccountSnapshotHandler accountSnapshotHandler;
    @Autowired AccountDBHandler accountDBHandler;

    @Override public void validate(ActionData actionData) {
        log.info("[accountOperation.validate] is start");
        process(actionData,TxProcessTypeEnum.VALIDATE);
        log.info("[accountOperation.validate] is success");
    }

    @Override public void persist(ActionData actionData) {
        log.info("[accountOperation.persist] is start");
        process(actionData,TxProcessTypeEnum.PERSIST);
        log.info("[accountOperation.persist] is success");
    }
    /**
     * process by type
     *
     * @param actionData
     * @param processTypeEnum
     */
    private void process(ActionData actionData,TxProcessTypeEnum processTypeEnum){
        log.info("[accountOperation.validate] is start");
        AccountOperation bo = (AccountOperation)actionData.getCurrentAction();
        if (bo == null) {
            log.error("[accountOperation.validate] convert action to AccountOperation is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        // validate param
        BeanValidateResult validateResult = BeanValidator.validate(bo);
        if (!validateResult.isSuccess()) {
            log.error("[accountOperation.validate] param validate is fail,first msg:{}", validateResult.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        AccountHandler accountHandler = null;
        if(processTypeEnum == TxProcessTypeEnum.VALIDATE){
            accountHandler = accountSnapshotHandler;
        }else if(processTypeEnum == TxProcessTypeEnum.PERSIST){
            accountHandler = accountDBHandler;
        }
        //validate
        Profiler.enter("[validateForOperation]");
        accountHandler.validateForOperation(bo,actionData.getCurrentTransaction().getCoreTx().getPolicyId());
        Profiler.release();
        //persist
        Profiler.enter("[persistForOperation]");
        accountHandler.persistForOperation(bo,actionData.getCurrentBlock().getBlockHeader().getHeight());
        Profiler.release();
    }
}
