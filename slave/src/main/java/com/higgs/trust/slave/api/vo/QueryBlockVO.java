package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tangfashuang
 */
@Getter
@Setter
public class QueryBlockVO extends BaseBO {

    private Long height;

    private String blockHash;

    private Integer pageNum;

    private Integer pageSize;
}
