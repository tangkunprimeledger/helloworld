package com.higgs.trust.slave.model.bo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description: signed transaction class
 * @author: pengdi
 **/
@Getter @Setter public class SignedTransaction extends BaseBO {

    private static final long serialVersionUID = -7372870730463030762L;

    /**
     * service transaction
     */
    @NotNull @Valid private CoreTransaction coreTx;

    /**
     * the list that store signatures
     */
    @NotEmpty private List<SignInfo> signatureList;

}
