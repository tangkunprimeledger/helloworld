package com.higgs.trust.slave.model.bo;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author WangQuanzhou
 * @desc core transaction class
 * @date 2018/3/26 16:59
 */
@Getter
@Setter
public class CoreTransaction extends BaseBO {

    private static final long serialVersionUID = -8957262691789681491L;

    /**
     * transaction id
     */
    @NotBlank
    @Length(max = 64)
    private String txId;

    /**
     * policy id
     */
    @NotBlank
    @Length(max = 32)
    private String policyId;

    /**
     * the list that store actions
     */
    @Valid
    private List<Action> actionList;

    /**
     * the JSONObject that store data which will be packaged into blockchain .It  can be null , when there is no need for it
     */
    private JSONObject bizModel;

    /**
     * lock time
     */
    private Date lockTime;

    /**
     * the tx sender's rsId
     */
    @NotBlank
    private String sender;
    /**
     * transaction version
     */
    @NotBlank
    private String version;


}
