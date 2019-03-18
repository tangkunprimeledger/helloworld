package com.higgs.trust.presstest.vo;

import com.higgs.trust.rs.core.vo.BaseVO;
import com.higgs.trust.slave.model.bo.account.AccountTradeInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-08-31
 */
@Getter
@Setter
public class AccountingVO extends BaseVO{
    /**
     * 请求号
     */
    private String reqNo;
    /**
     * 借记交易
     */
    private List<AccountTradeInfo> debitTradeInfo;
    /**
     * 贷记交易
     */
    private List<AccountTradeInfo> creditTradeInfo;
}
