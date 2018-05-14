package com.higgs.trust.slave.model.bo.utxo;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * UTXO
 *
 * @author lingchao
 * @create 2018年04月23日23:08
 */
@Getter
@Setter
public class UTXO extends BaseBO{
    /**
     * transaction id
     */
    private String txId;
    /**
     * index for the out in the transaction
     */
    private Integer index;
    /**
     * index for the action of the out in the transaction
     */
    private Integer actionIndex;
    /**
     * identity id for the attribution of the row:data owner and chain owner
     */
    private String identity;
    /**
     * the state class name
     */
    private String stateClass;
    /**
     * sate data
     */
    private JSONObject state;
    /**
     * contract address
     */
    private String contractAddress;
    /**
     * the status of the out: 1.UNSPENT 2.SPENT
     */
    private String status;
    /**
     * the transaction id to spend the out
     */
    private String sTxId;
    /**
     * create time
     */
    private Date createTime;
    /**
     * update time
     */
    private Date updateTime;
}
