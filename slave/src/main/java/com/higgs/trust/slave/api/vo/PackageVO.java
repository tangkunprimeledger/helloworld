package com.higgs.trust.slave.api.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/04/11 19:41
 * @desc receive from master or master send other node
 */
@Getter @Setter public class PackageVO extends BaseBO {
    /**
     * transaction list
     */
    @NotEmpty private List<SignedTransaction> signedTxList;

    /**
     * create package time
     */
    @NotNull private Long packageTime;

    /**
     * block height
     */
    @NotNull private Long height;

    /**
     * term
     */
    private Long term;

    /**
     * master name
     */
    private String masterName;

    /**
     * signature
     */
    @NotEmpty @JSONField(label = "sign") private String sign;
}
