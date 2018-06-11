package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ContractApiService;
import com.higgs.trust.contract.ExecuteContext;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.action.account.AccountUnFreezeHandler;
import com.higgs.trust.slave.model.bo.account.AccountUnFreeze;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * contract context service
 * @author duhongming
 * @date 2018-04-17
 */
@Slf4j @Service public class StandardContractContextService extends ContractApiService {
    @Autowired AccountUnFreezeHandler accountUnFreezeHandler;

    @Override
    public ExecuteContext getContext() {
        return super.getContext();
    }
    /**
     * execute unfreeze by js
     *
     * @param bizFlowNo
     * @param accountNo
     * @param amount
     * @param remark
     */
    public void unFreeze(String bizFlowNo,String accountNo,BigDecimal amount,String remark){
        AccountUnFreeze bo = new AccountUnFreeze();
        bo.setType(ActionTypeEnum.UNFREEZE);
        bo.setIndex(1);
        bo.setBizFlowNo(bizFlowNo);
        bo.setAccountNo(accountNo);
        bo.setAmount(amount);
        bo.setRemark(remark);

        ActionData actionData = getContextData(StandardExecuteContextData.class).getAction();

        accountUnFreezeHandler.unFreeze(bo,actionData.getCurrentBlock().getBlockHeader().getHeight());
    }
}
