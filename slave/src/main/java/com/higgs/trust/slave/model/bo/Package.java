package com.higgs.trust.slave.model.bo;

import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description:
 * @author: pengdi
 **/
@Getter @Setter public class Package extends BaseBO {

    /**
     * transaction list
     */
    @NotNull @Valid private List<SignedTransaction> signedTxList;

    /**
     * create package time
     */
    @NotNull private Long packageTime;

    /**
     * block height
     */
    @NotNull private Long height;

    /**
     * package status
     */
    private PackageStatusEnum status;
}
