package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.contract.StandardExecuteContextData;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Slf4j @Component public class ContractInvokeHandler implements ActionHandler {

    @Autowired private StandardSmartContract smartContract;

    private void check(ContractInvokeAction action) {
        if (null == action) {
            log.error("invokeContract validate >>  convert action to ContractInvokeAction is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (StringUtils.isEmpty(action.getAddress())) {
            log.error("invokeContract validate >> address is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }

    private void process(ActionData actionData, TxProcessTypeEnum processType) {
        ContractInvokeAction invokeAction = (ContractInvokeAction) actionData.getCurrentAction();
        this.check(invokeAction);
        ExecuteContextData data = new StandardExecuteContextData().put("ActionData", actionData);
        smartContract.execute(invokeAction.getAddress(), data, processType, invokeAction.getArgs());
    }

    @Override public void validate(ActionData actionData) {
        process(actionData, TxProcessTypeEnum.VALIDATE);
    }

    @Override public void persist(ActionData actionData) {
        process(actionData, TxProcessTypeEnum.PERSIST);
    }
}
