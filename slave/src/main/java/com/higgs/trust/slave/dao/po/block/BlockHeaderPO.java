package com.higgs.trust.slave.dao.po.block;import com.higgs.trust.slave.dao.po.BaseEntity;import lombok.Getter;import lombok.Setter;import java.util.Date;@Getter @Setter public class BlockHeaderPO extends BaseEntity<BlockHeaderPO> {    /**     * block height     */    private Long height;    /**     * the header type     */    private String type;    /**     * block p2p data     */    private String headerData;    /**     * create time     */    private Date createTime;}