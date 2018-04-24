package com.higgs.trust.slave.dao.po.merkle;

import com.higgs.trust.slave.dao.po.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author WangQuanzhou
 * @desc merkle tree po
 * @date 2018/3/29 16:30
 */
@Getter @Setter public class MerkleTreePO extends BaseEntity {

    private static final long serialVersionUID = -8977162340234152348L;
    // Merkle Root
    private String rootHash;

    // the total level of the merkle tree
    private int totalLevel;

    // the max index of leaf level of the merkle tree
    private Long maxIndex;

    // type of merkle tree，can be ACCOUNT or TX or CONTRACT
    private String treeType;

    // create time
    private Date createTime;

    // update time
    private Date updateTime;

    public MerkleTreePO() {
    }

    public MerkleTreePO(String rootHash, int totalLevel, Long maxIndex, String treeType) {
        this.rootHash = rootHash;
        this.totalLevel = totalLevel;
        this.maxIndex = maxIndex;
        this.treeType = treeType;
    }
}
