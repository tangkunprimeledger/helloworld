package com.higgs.trust.slave.model.bo.account;import com.higgs.trust.slave.model.bo.BaseBO;import lombok.Getter;import lombok.Setter;import java.util.Date;/** * @author liuyu * @description currency info * @date 2018-03-27 */@Getter @Setter public class CurrencyInfo extends BaseBO {    /**     * id     */    private Long id;    /**     * currency     */    private String currency;    /**     * remark     */    private String remark;    /**     * create time     */    private Date createTime;    /**     * homomorphic public key     */    private String homomorphicPk;}