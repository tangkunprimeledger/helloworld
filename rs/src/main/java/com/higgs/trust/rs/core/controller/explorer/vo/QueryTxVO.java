package com.higgs.trust.rs.core.controller.explorer.vo;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author liuyu
 * @description
 * @date 2018-07-25
 */
@Getter @Setter public class QueryTxVO extends BaseBO {
    @NotNull
    @Size(max = 64)
    private String txId;
}
