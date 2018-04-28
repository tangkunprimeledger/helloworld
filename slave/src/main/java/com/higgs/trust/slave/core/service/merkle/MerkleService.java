package com.higgs.trust.slave.core.service.merkle;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;

import java.util.List;

/**
 * @author WangQuanzhou
 * @desc merkle tree service interface
 * @date 2018/4/10 16:28
 */
public interface MerkleService {
    /**
     * create a merkle tree
     *
     * @param type
     * @param dataList
     * @return
     */
    MerkleTree build(MerkleTypeEnum type, List<Object> dataList);

    /**
     * update a merkle tree
     *
     * @param merkleTree
     * @param objOld
     * @param objNew
     */
    void update(MerkleTree merkleTree, Object objOld, Object objNew);

    /**
     * insert one node into a merkle tree
     *
     * @param merkleTree
     * @param obj
     */
    void add(MerkleTree merkleTree, Object obj);

    /**
     * flush merkle tree info into database
     *
     * @param merkleTree
     */
    void flush(MerkleTree merkleTree);

    /**
     * query a merkle tree with the exact type
     *
     * @param treeType
     * @return
     */
    MerkleTree queryMerkleTree(MerkleTypeEnum treeType);

}
