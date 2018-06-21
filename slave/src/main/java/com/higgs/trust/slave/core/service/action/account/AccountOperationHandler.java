package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.service.action.ActionHandler;
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
    /**
     * process by type
     *
     * @param actionData
     */
    @Override public void process(ActionData actionData){
        log.debug("[accountOperation.validate] is start");
        AccountOperation bo = (AccountOperation)actionData.getCurrentAction();
        //validate
        Profiler.enter("[validateForOperation]");
        accountSnapshotHandler.validateForOperation(bo,actionData.getCurrentTransaction().getCoreTx().getPolicyId());
        Profiler.release();
        //persist
        Profiler.enter("[persistForOperation]");
        accountSnapshotHandler.persistForOperation(bo,actionData.getCurrentBlock().getBlockHeader().getHeight());
        Profiler.release();
    }
}
