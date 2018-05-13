package com.higgs.trust.rs.custom.vo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
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
    @NotBlank
    @Length(max = 64)
    private String requestId;

    /**
     * 业务存证模型 4096
     */
    @NotBlank
    private String bizModel;

    /**
     * 票据编号 64
     */
    @NotBlank
    @Length(max = 64)
    private String billId;

    /**
     * 承兑人 64
     */
    @NotBlank
    @Length(max = 64)
    private String finalPayerId;

    /**
     * 金额
     */
    @NotNull
    private BigDecimal amount;

    /**
     * 到期日
     */
    @NotBlank
    private String dueDate;

    /**
     * 持票人
     */
    @NotBlank
    @Length(max = 64)
    private String holder;
}
