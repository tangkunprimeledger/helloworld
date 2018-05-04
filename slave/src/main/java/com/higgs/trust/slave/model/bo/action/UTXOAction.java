package com.higgs.trust.slave.model.bo.action;

import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * UTXO action
 *
 * @author lingchao
 * @create 2018年03月27日21:09
 */
@Getter @Setter public class UTXOAction extends Action {

    /**
     * tx inputs list
     */
    @Valid private List<TxIn> inputList;
    /**
     * tx outputs list
     */
    @Valid private List<TxOut> outputList;
    /**
     * tx state class name
     */
    @NotBlank @Length(max = 255) private String stateClass;
    /**
     * tx contract script
     */
    @NotNull private String contract;
    /**
     * tx utxo action type
     * 1.ISSUE (all  should sign the tx,the tx likes no one input and at least one output)
     * 2.NORMAL (partners should should sign the tx, the tx likes  at least one input and at least one output)
     * 3.DESTRUCTION(all  should sign the tx, the tx likes at least one input and no one out put)
     */
    @NotNull private UTXOActionTypeEnum utxoActionType;

}
