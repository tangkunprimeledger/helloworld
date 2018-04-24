package com.higgs.trust.slave.model.bo.utxo;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * TxOutPO BO
 *
 * @author lingchao
 * @create 2018年03月27日21:22
 */
@Getter @Setter public class TxOut extends BaseBO {
    /**
     * index
     */
    @NotNull private Integer index;
    /**
     * action index
     */
    @NotNull private Integer actionIndex;
    /**
     * identityId
     */
    @NotBlank private String identity;
    /**
     * sate data
     */
    @NotNull private JSONObject state;

}
