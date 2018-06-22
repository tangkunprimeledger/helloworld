package com.higgs.trust.rs.tx;

import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import org.junit.Test;
import org.testng.collections.Lists;

/**
 * @author liuyu
 * @description
 * @date 2018-06-22
 */
public class AccountTxTest{

    /**
     * 创建币种
     */
    @Test
    public void testCreateCurrency(){
        IssueCurrency action = new IssueCurrency();
        action.setIndex(1);
        action.setType(ActionTypeEnum.ISSUE_CURRENCY);
        action.setCurrencyName("CNY-A");
        action.setRemark("for test");
        RsCoreTxVO rsCoreTxVO = CoreTxHelper.makeSimpleTx("create_currency_" + System.currentTimeMillis(), Lists.newArrayList(action));
        CoreTxHelper.post(rsCoreTxVO);
    }
}
