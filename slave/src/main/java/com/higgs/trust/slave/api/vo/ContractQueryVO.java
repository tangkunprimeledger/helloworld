package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author duhongming
 * @date 2018/5/16
 */
@Getter
@Setter
public class ContractQueryVO extends BaseBO {
    private Long height;
    private String txId;
    private Integer pageIndex;
    private Integer pageSize;
}
