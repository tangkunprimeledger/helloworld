package com.higgs.trust.slave.dao.po.account;import com.higgs.trust.slave.dao.po.BaseEntity;import lombok.Getter;import lombok.Setter;import java.math.BigDecimal;import java.util.Date;/** * @author liuyu * @description account info * @date 2018-03-27 */@Getter @Setter public class AccountInfoPO extends BaseEntity<AccountInfoPO> {    /**     * id     */    private Long id;    /**     * number of account     */    private String accountNo;    /**     * currency of account     */    private String currency;    /**     * balance of account     */    private BigDecimal balance;    /**     * freeze amount of account     */    private BigDecimal freezeAmount;    /**     * fund direction-DEBIT,CREDIT     */    private String fundDirection;    /**     * detail number     */    private Long detailNo;    /**     * freeze detail number     */    private Long detailFreezeNo;    /**     * status,NORMAL,DESTROY     */    private String status;    /**     * create time     */    private Date createTime;    /**     * update time     */    private Date updateTime;}