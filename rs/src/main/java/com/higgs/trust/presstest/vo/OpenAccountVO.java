package com.higgs.trust.presstest.vo;

import com.higgs.trust.rs.core.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyu
 * @description
 * @date 2018-08-31
 */
@Getter
@Setter
public class OpenAccountVO extends BaseVO{
    /**
     * 请求号
     */
    private String reqNo;
    /**
     * 账号
     */
    private String accountNo;
    /**
     * 币种
     */
    private String currencyName;
    /**
     * 余额方向
     * 0：借
     * 1：贷
     */
    private int fundDirection;
}
