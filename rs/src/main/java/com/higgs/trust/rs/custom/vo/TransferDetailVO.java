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
@Getter @Setter public class TransferDetailVO {
    /**
     * 持票人 64
     */
    @NotBlank @Length(max = 64) private String nextHolder;

    /**
     * 金额
     */
    @NotNull private BigDecimal amount;

}
