package com.higgs.trust.slave.dao.po.transaction;import com.higgs.trust.slave.dao.po.BaseEntity;import lombok.Getter;import lombok.Setter;import java.util.Date;/** * tx po * * @author lingchao * @create 2018年03月27日19:19 */@Getter @Setter public class TransactionPO extends BaseEntity<TransactionPO> {    private static final long serialVersionUID = 1L;    /**     * transaction id     */    private String txId;    /**     * the save data of the core     */    private Object bizModel;    /**     * policy id     */    private String policyId;    /**     * the lock time of the tx . use in rs and slave to deal tx     */    private Date lockTime;    /**     * the version of the tx     */    private String version;    /**     * the block height of the tx     */    private Long blockHeight;    /**     * the create time of the block for the tx     */    private Date blockTime;    /**     * the tx sender's rsId     */    private String sender;    /**     * the signed datas by json     */    private String signDatas;    /**     * action list     */    private String actionDatas;    /**     * the tx execute result 0:fail 1:success     */    private String executeResult;    /**     * execute error code     */    private String errorCode;}