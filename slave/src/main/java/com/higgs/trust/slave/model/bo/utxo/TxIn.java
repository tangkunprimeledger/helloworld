package com.higgs.trust.slave.model.bo.utxo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * TxIn bo
 *
 * @author lingchao
 * @create 2018年03月27日21:20
 */
@Getter @Setter public class TxIn extends BaseBO {

    /**
     * tx ID
     */
    @NotBlank @Length(max = 64) private String txId;
    /**
     * index
     */
    @NotNull private Integer index;
    /**
     * action index
     */
    @NotNull private Integer actionIndex;

    @Override
    public String toString(){
        return txId + index + actionIndex;
    }

}
