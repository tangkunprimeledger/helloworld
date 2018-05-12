package com.higgs.trust.rs.core.bo;import com.alibaba.fastjson.JSONObject;import com.higgs.trust.rs.common.BaseBO;import com.higgs.trust.rs.common.enums.BizTypeEnum;import com.higgs.trust.slave.api.enums.VersionEnum;import com.higgs.trust.slave.model.bo.action.Action;import lombok.Getter;import lombok.Setter;import javax.validation.Valid;import javax.validation.constraints.NotNull;import java.util.Date;import java.util.List;@Getter @Setter public class CoreTxBO extends BaseBO {    /**     * transaction id     */    @NotNull private String txId;    /**     * the business type     */    @NotNull private BizTypeEnum bizType;    /**     * the storage data     */    private JSONObject bizModel;    /**     * policy id     */    @NotNull private String policyId;    /**     * the lock time create the tx . use in rs and slave to deal tx     */    private Date lockTime;    /**     * the version create the tx     */    @NotNull private VersionEnum version;    /**     * actions     */    @Valid private List<Action> actionDatas;    /**     * the signature for tx     */    @NotNull List<String> signDatas;}