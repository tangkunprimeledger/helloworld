package com.higgs.trust.slave.model.bo.merkle;

import com.higgs.trust.slave.api.enums.MerkleStatusEnum;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author WangQuanzhou
 * @desc Merkle Node BO
 * @date 2018/4/10 14:27
 */
@Getter @Setter
public class MerkleNode extends BaseBO {
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

    // type of merkle tree，can be ACCOUNT or TX or CONTRACT
    private MerkleTypeEnum treeType;

    // does this merkle node have changed
    // can be NO_CHANGE or ADD or MODIFY, default NO_CHANGE
    private MerkleStatusEnum status = MerkleStatusEnum.NO_CHANGE;

    // default constructor
    public MerkleNode() {
    }

    // constructor
    public MerkleNode(String nodeHash, String uuid, long index, int level, String parent, MerkleTypeEnum treeType,
        MerkleStatusEnum status) {
        this.nodeHash = nodeHash;
        this.uuid = uuid;
        this.index = index;
        this.level = level;
        this.parent = parent;
        this.treeType = treeType;
        this.status = status;
    }
}
