package com.higgs.trust.presstest.vo;

import com.higgs.trust.rs.core.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyu
 * @description
 * @date 2018-08-30
 */
@Getter @Setter public class CurrencyVO extends BaseVO {
    /**
     * 请求号
     */
    private String reqNo;
    /**
     * 币种名称
     */
    private String currencyName;
    /**
     * 备注
     */
    private String remark;
}
