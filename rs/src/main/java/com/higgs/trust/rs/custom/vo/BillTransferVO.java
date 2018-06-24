package com.higgs.trust.rs.custom.vo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.List;

/**
 * @Description: 票据转让
 * @author: pengdi
 **/
@Getter @Setter public class BillTransferVO {
    /**
     * 请求编号 64
     */
    @NotBlank @Length(max = 64) private String requestId;

    /**
     * 业务存证模型 8192
     */
    @Length(max = 8192) private String bizModel;

    /**
     * 应收票据编号 64
     */
    @NotBlank @Length(max = 64) private String billId;

    /**
     * 持票人 64
     */
    @NotBlank @Length(max = 64) private String holder;

    /**
     * 受让持票人 64
     */
    @Valid @NotEmpty
    private List<TransferDetailVO> transferList;
}
