package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.account.AccountSnapshotHandler;
import com.higgs.trust.slave.model.bo.account.AccountOperation;
import com.higgs.trust.slave.model.bo.account.AccountTradeInfo;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author liuyu
 * @description account operation datahandler
 * @date 2018-03-28
 */
@Slf4j @Component public class AccountOperationHandler implements ActionHandler {
    @Autowired AccountSnapshotHandler accountSnapshotHandler;

    @Override public void verifyParams(Action action) throws SlaveException {
        AccountOperation bo = (AccountOperation)action;
        if(StringUtils.isEmpty(bo.getBizFlowNo()) || bo.getBizFlowNo().length() > 64){
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(bo.getAccountDate() == null){
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(CollectionUtils.isEmpty(bo.getCreditTradeInfo()) || CollectionUtils.isEmpty(bo.getDebitTradeInfo())){
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        for(AccountTradeInfo tradeInfo : bo.getCreditTradeInfo()){
            if(tradeInfo == null || StringUtils.isEmpty(tradeInfo.getAccountNo())){
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
            if(tradeInfo.getAmount() == null){
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
        }
        for(AccountTradeInfo tradeInfo : bo.getDebitTradeInfo()){
            if(tradeInfo == null || StringUtils.isEmpty(tradeInfo.getAccountNo())){
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
            if(tradeInfo.getAmount() == null){
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
        }
    }

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
