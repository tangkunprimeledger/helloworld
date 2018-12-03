package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractStateSnapshotAgent;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.ContractStateMigrationAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/6/21
 */
@Slf4j
@Component
public class ContractStateMigrationHandler implements ActionHandler {

    @Autowired
    private ContractStateSnapshotAgent contractStateSnapshotAgent;

    private void checkAction(ContractStateMigrationAction action) {
        if (StringUtils.isEmpty(action.getFormInstanceAddress())) {
            log.error("[verifyParams] formInstanceAddress is null or illegal param:{}",action);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (StringUtils.isEmpty(action.getToInstanceAddress())) {
            log.error("[verifyParams] toInstanceAddress is null or illegal param:{}",action);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (action.getFormInstanceAddress().equals(action.getToInstanceAddress())) {
            log.error("[verifyParams] can't  migration state to self. param:{}",action);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }

    @Override public void verifyParams(Action action) throws SlaveException {
        ContractStateMigrationAction bo = (ContractStateMigrationAction) action;
        checkAction(bo);
    }

    @Override
    public void process(ActionData actionData) {
        ContractStateMigrationAction action = (ContractStateMigrationAction) actionData.getCurrentAction();
        checkAction(action);
        List<String> state = (List<String>)contractStateSnapshotAgent.get(action.getFormInstanceAddress());
        if (state == null || state.isEmpty()) {
            throw new RuntimeException("can't migration empty state.");
        }
        List<String> toState = (List<String>)contractStateSnapshotAgent.get(action.getToInstanceAddress());
        if (toState != null && !toState.isEmpty()) {
            throw new RuntimeException(String.format("can't migration state to %s, it already have state.", action.getToInstanceAddress()));
        }
        //state transfer
        for(String key : state){
            Object data = contractStateSnapshotAgent.get(key);
            //make new key by new address
            key = key.replaceAll(action.getFormInstanceAddress(),action.getToInstanceAddress());
            //reset value by new key
            contractStateSnapshotAgent.put(key, data);
        }
    }
}
