package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.contract.StateManager;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractStateSnapshotAgent;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.ContractStateMigrationAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            throw new RuntimeException("formInstanceAddress is null.");
        }
        if (StringUtils.isEmpty(action.getToInstanceAddress())) {
            throw new RuntimeException("toInstanceAddress is null.");
        }
        if (action.getFormInstanceAddress().equals(action.getToInstanceAddress())) {
            throw new RuntimeException("can't  migration state to self.");
        }
    }


    @Override
    public void process(ActionData actionData) {
        ContractStateMigrationAction action = (ContractStateMigrationAction) actionData.getCurrentAction();
        checkAction(action);

        StateManager stateManager = contractStateSnapshotAgent.get(action.getFormInstanceAddress());
        if (stateManager.getState().size() == 0) {
            throw new RuntimeException("can't migration empty state.");
        }

        StateManager toStateManager = contractStateSnapshotAgent.get(action.getToInstanceAddress());
        if (toStateManager.getState().size() > 0) {
            throw new RuntimeException(String.format("can't migration state to %s, it already have state.", action.getToInstanceAddress()));
        }

        contractStateSnapshotAgent.put(action.getToInstanceAddress(), stateManager);
    }
}
