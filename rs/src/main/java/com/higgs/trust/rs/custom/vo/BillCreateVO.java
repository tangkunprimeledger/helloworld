package com.higgs.trust.rs.custom.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Description:
 * @author: pengdi
 **/
@Getter
@Setter
public class BillCreateVO {
    /**
     * 请求编号 64
     */
    private String requestId;

    /**
     * 业务存证模型 4096
     */
    private String bizModel;

    /**
     * 票据编号 64
     */
    private String billId;

    /**
     * 承兑人 64
     */
    private String finalPayerId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 到期日
     */
    private String dueDate;

    /**
     * 持票人
     */
    private String holder;
}
