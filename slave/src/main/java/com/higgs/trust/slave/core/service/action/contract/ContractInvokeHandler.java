package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.contract.StandardExecuteContextData;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author duhongming
 * @date 2018/05/07
 */
@Slf4j @Component public class ContractInvokeHandler implements ActionHandler {

    @Autowired private StandardSmartContract smartContract;

    private void check(ContractInvokeAction action) {
        if (StringUtils.isEmpty(action.getAddress())) {
            log.error("invokeContract validate: address is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (action.getAddress().length() > 64) {
            log.error("invokeContract validate: address is too long");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }

    private void processInternal(ActionData actionData) {
        if (!(actionData.getCurrentAction() instanceof ContractInvokeAction)) {
            throw new IllegalArgumentException("action need a type of ContractInvokeAction");
        }
        ContractInvokeAction invokeAction = (ContractInvokeAction)actionData.getCurrentAction();
        this.check(invokeAction);
        ExecuteContextData data = new StandardExecuteContextData().setAction(actionData);
        smartContract.execute(invokeAction.getAddress(), data, invokeAction.getArgs());
    }

    @Override public void verifyParams(Action action) throws SlaveException {
        ContractInvokeAction invokeAction = (ContractInvokeAction)action;
        check(invokeAction);
    }

    @Override public void process(ActionData actionData) {
        log.debug("contract invoke start");
        Profiler.enter("contract invoke");
        try {
            processInternal(actionData);
        } finally {
            Profiler.release();
        }
    }
}
