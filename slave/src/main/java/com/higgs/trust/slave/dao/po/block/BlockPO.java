package com.higgs.trust.slave.dao.po.block;import com.higgs.trust.common.mybatis.BaseEntity;import com.higgs.trust.slave.dao.po.transaction.TransactionPO;import com.higgs.trust.slave.model.bo.SignedTransaction;import lombok.Getter;import lombok.Setter;import java.math.BigDecimal;import java.util.Date;import java.util.List;@Getter @Setter public class BlockPO extends BaseEntity<BlockPO> {    /**     * block height     */    private Long height;    /**     * version     */    private String version;    /**     * previous block hash     */    private String previousHash;    /**     * block hash     */    private String blockHash;    /**     * transcation merkel tree root hash     */    private String txRootHash;    /**     * account state merkel tree root hash     */    private String accountRootHash;    /**     * contract state merkel tree root hash     */    private String contractRootHash;    /**     * policy merkel tree root hash     */    private String policyRootHash;    /**     * rs merkel tree root hash     */    private String rsRootHash;    /**     * tx receipt merkel tree root hash     */    private String txReceiptRootHash;    /**     * ca merkle tree root hash     */    private String caRootHash;    /**     * block time     */    private Date blockTime;    /**     * transaction number     */    private Integer txNum;    /**     * the number of transactions recorded by the current block     */    private Long totalTxNum;    /**     * total block size,unit:kb     */    private BigDecimal totalBlockSize;    /**     * the list that store signed transaction     */    private List<SignedTransaction> signedTxs;    /**     * create time     */    private Date createTime;}