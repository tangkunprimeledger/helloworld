package com.higgs.trust.slave.dao.po.merkle;

import com.higgs.trust.slave.dao.po.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author WangQuanzhou
 * @desc merkle node po
 * @date 2018/4/8 17:11
 */
@Getter @Setter public class MerkleNodePO extends BaseEntity {

    private static final long serialVersionUID = 4405343475978421701L;
    // merkle node hash
    private String nodeHash;

    // the unique id of merkle node
    private String uuid;

    // the index in the current level
    private long index;

    // current level
    private int level;

    // current level
    private String parent;

    // type of merkle tree，should be ACCOUNT or TX or CONTRACT
    private String treeType;

    // create time
    private Date createTime;

    // update time
    private Date updateTime;

    public MerkleNodePO() {
    }

    public MerkleNodePO(String nodeHash, String uuid, long index, int level, String parent, String treeType) {
        this.nodeHash = nodeHash;
        this.uuid = uuid;
        this.index = index;
        this.level = level;
        this.parent = parent;
        this.treeType = treeType;
    }
}
