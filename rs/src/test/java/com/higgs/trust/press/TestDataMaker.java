package com.higgs.trust.press;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.presstest.AppConst;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.account.*;
import com.higgs.trust.slave.model.bo.action.Action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author liuyu
 * @description
 * @date 2018-04-17
 */
public class TestDataMaker {

    public static Action makeOpenAccountAction(String accountNo, int index, FundDirectionEnum fundDirectionEnum) {
        OpenAccount action = new OpenAccount();
        action.setType(ActionTypeEnum.OPEN_ACCOUNT);
        action.setIndex(index);
        action.setAccountNo(accountNo);
        action.setChainOwner(AppConst.CHAIN_OWNER);
        action.setDataOwner(AccountingService.SENDER);
        action.setCurrency("CNY");
        action.setFundDirection(fundDirectionEnum);
        return action;
    }

    public static Action makeFreezeAction(String accountNo, String bizFlowNo, BigDecimal amount) {
        AccountFreeze action = new AccountFreeze();
        action.setType(ActionTypeEnum.FREEZE);
        action.setAccountNo(accountNo);
        action.setAmount(amount);
        action.setBizFlowNo(bizFlowNo);
        return action;
    }

    public static Action makeUnFreezeAction(String accountNo, String bizFlowNo, BigDecimal amount) {
        AccountUnFreeze action = new AccountUnFreeze();
        action.setType(ActionTypeEnum.UNFREEZE);
        action.setAccountNo(accountNo);
        action.setAmount(amount);
        action.setBizFlowNo(bizFlowNo);
        return action;
    }

    public static Action makeOpertionAction(String debitAccountNo, String creditAccountNo, BigDecimal happenAmount) {
        AccountOperation action = new AccountOperation();
        action.setType(ActionTypeEnum.ACCOUNTING);
        action.setIndex(0);

        List<AccountTradeInfo> debitTradeInfo = new ArrayList<>();
        debitTradeInfo.add(new AccountTradeInfo(debitAccountNo, happenAmount));
        List<AccountTradeInfo> creditTradeInfo = new ArrayList<>();
        creditTradeInfo.add(new AccountTradeInfo(creditAccountNo, happenAmount));

        action.setBizFlowNo("biz_flow_no_" + System.currentTimeMillis() + new Random().nextInt(10000));
        action.setDebitTradeInfo(debitTradeInfo);
        action.setCreditTradeInfo(creditTradeInfo);
        action.setAccountDate(new Date());

        return action;
    }

    public static CoreTransaction makeCoreTx(List<Action> actions, int index, String policyId, JSONObject bizModel) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setPolicyId(policyId);
        coreTx.setTxId(
            "tx_id_" + actions.get(0).getType().getCode() + "_" + index + "_" + System.currentTimeMillis() + Thread
                .currentThread().getName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setActionList(actions == null ? new ArrayList<>() : actions);
        coreTx.setBizModel(bizModel);
        coreTx.setSender(AccountingService.SENDER);
        coreTx.setLockTime(null);
        coreTx.setSendTime(new Date());
        return coreTx;
    }
}
