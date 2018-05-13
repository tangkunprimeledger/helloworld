package com.higgs.trust.rs.custom.vo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @Description:
 * @author: pengdi
 **/
@Getter
@Setter
public class BillTransferVO {
    /**
     * 请求编号 64
     */
    @NotBlank
    @Length(max = 64)
    private String requestId;

    /**
     * 业务存证模型 8192
     */
    @NotBlank
    @Length(max = 8192)
    private String bizModel;

    /**
     * 应收票据编号 64
     */
    @NotBlank
    @Length(max = 64)
    private String billId;

    /**
     * 受让持票人 64
     */
    @NotBlank
    @Length(max = 64)
    private String nextHolder;
}
