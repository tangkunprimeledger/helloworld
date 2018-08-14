package com.higgs.trust.rs.tx;

import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.account.*;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import org.junit.Test;
import org.testng.collections.Lists;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-06-22
 */
public class AccountTxTest {


    @Test
    public void test(){
        testCreateCurrency();
//        testOpenAccount();
//        testIn();
//        testOut();
//        testTransafer();
//        testFreeze();
//        testUnFreeze();
    }

    /**
     * 创建币种
     */
    public void testRegister() {
        RegisterRS registerRS = new RegisterRS();
        registerRS.setIndex(0);
        registerRS.setType(ActionTypeEnum.REGISTER_RS);
        registerRS.setRsId("test-rs-1");
        registerRS.setDesc("测试RS注册");
        CoreTransaction rsCoreTxVO =
            CoreTxHelper.makeSimpleTx("tx_regist_rs_" + System.currentTimeMillis(), Lists.newArrayList(registerRS));
        CoreTxHelper.post(rsCoreTxVO);
    }

    /**
     * 创建币种
     */
    public void testCreateCurrency() {
        IssueCurrency action = new IssueCurrency();
        action.setIndex(1);
        action.setType(ActionTypeEnum.ISSUE_CURRENCY);
        action.setCurrencyName("CNY-A");
        action.setRemark("for test");
        CoreTransaction rsCoreTxVO =
            CoreTxHelper.makeSimpleTx("tx_create_currency_" + System.currentTimeMillis(), Lists.newArrayList(action));
        CoreTxHelper.post(rsCoreTxVO);
    }

    /**
     * 开户
     */
    public void testOpenAccount() {
        List<Action> actionList = Lists.newArrayList(2);
        for (int i = 0; i < 1; i++) {
            OpenAccount action = new OpenAccount();
            action.setIndex(1);
            action.setType(ActionTypeEnum.OPEN_ACCOUNT);
            action.setCurrency("CNY");
            action.setAccountNo("account_no_t_0" + i);
            action.setFundDirection(i == 0 ? FundDirectionEnum.DEBIT : FundDirectionEnum.CREDIT);
            action.setDataOwner(CoreTxHelper.SENDER);
            action.setChainOwner(CoreTxHelper.SENDER);
            actionList.add(action);
        }
        CoreTransaction rsCoreTxVO =
            CoreTxHelper.makeSimpleTx("tx_open_account_" + System.currentTimeMillis(), actionList);
        CoreTxHelper.post(rsCoreTxVO);
    }

    /**
     * 入金
     */
    public void testIn() {
        AccountOperation action = new AccountOperation();
        action.setIndex(1);
        action.setType(ActionTypeEnum.ACCOUNTING);
        action.setBizFlowNo("biz_flow_no_" + System.currentTimeMillis());
        action.setDebitTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_t_0", new BigDecimal(100))));
        action.setCreditTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_t_1", new BigDecimal(100))));
        action.setAccountDate(new Date());
        action.setRemark("for test");
        CoreTransaction rsCoreTxVO =
            CoreTxHelper.makeSimpleTx("tx_accounting_in_" + System.currentTimeMillis(), Lists.newArrayList(action));
        CoreTxHelper.post(rsCoreTxVO);
    }

    /**
     * 出金
     */
    public void testOut() {
        AccountOperation action = new AccountOperation();
        action.setIndex(1);
        action.setType(ActionTypeEnum.ACCOUNTING);
        action.setBizFlowNo("biz_flow_no_" + System.currentTimeMillis());
        action.setDebitTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_t_1", new BigDecimal(10))));
        action.setCreditTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_t_0", new BigDecimal(10))));
        action.setAccountDate(new Date());
        action.setRemark("for test");
        CoreTransaction rsCoreTxVO =
            CoreTxHelper.makeSimpleTx("tx_accounting_out_" + System.currentTimeMillis(), Lists.newArrayList(action));
        CoreTxHelper.post(rsCoreTxVO);
    }

    /**
     * 转账
     */
    public void testTransafer() {
        AccountOperation action = new AccountOperation();
        action.setIndex(1);
        action.setType(ActionTypeEnum.ACCOUNTING);
        action.setBizFlowNo("biz_flow_no_" + System.currentTimeMillis());
        action.setDebitTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_t_0", new BigDecimal(10))));
        action.setCreditTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_t_00", new BigDecimal(10))));
        action.setAccountDate(new Date());
        action.setRemark("for test");
        CoreTransaction rsCoreTxVO = CoreTxHelper
            .makeSimpleTx("tx_accounting_transfer_" + System.currentTimeMillis(), Lists.newArrayList(action));
        CoreTxHelper.post(rsCoreTxVO);
    }

    /**
     * 冻结
     */
    public void testFreeze() {
        AccountFreeze action = new AccountFreeze();
        action.setIndex(1);
        action.setType(ActionTypeEnum.FREEZE);
        action.setBizFlowNo("biz_flow_no_freeze_002");
        action.setAccountNo("account_no_t_00");
        action.setAmount(new BigDecimal(10));
        action.setContractAddr("12345678");
        action.setRemark("for test");
        CoreTransaction rsCoreTxVO =
            CoreTxHelper.makeSimpleTx("tx_freeze_" + System.currentTimeMillis(), Lists.newArrayList(action));
        CoreTxHelper.post(rsCoreTxVO);
    }

    /**
     * 解冻
     */
    @Test public void testUnFreeze() {
        AccountUnFreeze action = new AccountUnFreeze();
        action.setIndex(1);
        action.setType(ActionTypeEnum.UNFREEZE);
        action.setBizFlowNo("biz_flow_no_freeze_002");
        action.setAccountNo("account_no_t_00");
        action.setAmount(new BigDecimal(0.1));
        action.setRemark("for test");
        CoreTransaction rsCoreTxVO =
            CoreTxHelper.makeSimpleTx("tx_unfreeze_" + System.currentTimeMillis(), Lists.newArrayList(action));
        CoreTxHelper.post(rsCoreTxVO);
    }
}
