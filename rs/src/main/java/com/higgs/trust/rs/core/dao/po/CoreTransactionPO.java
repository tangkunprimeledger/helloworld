package com.higgs.trust.rs.core.dao.po;import com.higgs.trust.common.mybatis.BaseEntity;import lombok.Getter;import lombok.Setter;import java.util.Date;@Getter @Setter public class CoreTransactionPO extends BaseEntity<CoreTransactionPO> {    private static final long serialVersionUID = 1L;    /**     * id     */    private Long id;    /**     * transaction id     */    private String txId;    /**     * the save data create the biz     */    private String bizModel;    /**     * policy id     */    private String policyId;    /**     * the lock time create the tx . use in rs and slave to deal tx     */    private Date lockTime;    /**     * the rsId if the sender  for the tx     */    private String sender;    /**     * the version create the tx     */    private String version;    /**     * the status create the row: 1.INIT 2.WAIT 3.VALIDATED 4.PERSISTED 5.END     */    private String status;    /**     * tx execute result,0:fail,1:success     */    private String executeResult;    /**     * tx execute error code     */    private String errorCode;    /**     * tx execute error msg     */    private String errorMsg;    /**     * the action datas for tx     */    private String actionDatas;    /**     * the signature for tx     */    private String signDatas;    /**     * transaction send time     */    private Date sendTime;    /**     * create time     */    private Date createTime;    /**     * update time     */    private Date updateTime;    /**     * block height     */    private Long blockHeight;}