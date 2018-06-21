package com.higgs.trust.rs.core.vo.account;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyu
 * @description
 * @date 2018-06-21
 */
@Getter @Setter public class CurrencyVO extends BaseBO{
    private String currency;
    private String remark;
}
