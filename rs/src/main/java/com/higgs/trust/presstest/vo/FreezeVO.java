package com.higgs.trust.presstest.vo;

import com.higgs.trust.rs.core.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author liuyu
 * @description
 * @date 2018-08-31
 */
@Getter
@Setter
public class FreezeVO extends BaseVO {
    /**
     * 请求号
     */
    private String reqNo;
    /**
     * 账号
     */
    private String accountNo;
    /**
     * 冻结流水号
     */
    private String bizFlowNo;
    /**
     * 冻结金额/Volumes/work/
     */
    private BigDecimal amount;
}
