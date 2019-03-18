package com.higgs.trust.slave.model.bo;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
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

    @Override public int hashCode() {
        if (coreTx != null && coreTx.getTxId() != null) {
            return coreTx.getTxId().hashCode();
        }
        return super.hashCode();
    }

    @Override public boolean equals(Object obj) {
        if (obj != null && obj instanceof SignedTransaction) {
            SignedTransaction signedTx = (SignedTransaction)obj;
            if (this.coreTx != null && signedTx.coreTx != null) {
                return StringUtils.equals(this.coreTx.getTxId(), signedTx.getCoreTx().getTxId());
            }
        }
        return super.equals(obj);
    }
}
