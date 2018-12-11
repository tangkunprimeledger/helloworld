package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author tangfashuang
 */
@Getter
@Setter
public class QueryBlockVO extends BaseBO {

    private Long height;

    @Size(max = 64)
    private String blockHash;

    @NotNull
    private Integer pageNo;

    @NotNull
    private Integer pageSize;
}
