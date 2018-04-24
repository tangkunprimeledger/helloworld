package com.higgs.trust.slave.dao.merkle;

import com.higgs.trust.slave.dao.po.merkle.MerkleNodePO;
import com.higgs.trust.slave.dao.po.merkle.MerkleTreePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author WangQuanzhou
 * @desc merkle tree CRUD
 * @date 2018/3/29 16:24
 */
@Mapper public interface MerkleDao {

    /**
     * insert merkle tree into db
     *
     * @param merkleTreePO
     */
    public void insertMerkleTree(MerkleTreePO merkleTreePO);

    /**
     * query merkle tree by treeType, blockHeight, rootHash(if not null)
     *
     * @param merkleTreePO
     * @return
     */
    public MerkleTreePO queryMerkleTree(MerkleTreePO merkleTreePO);

    /**
     * insert merkle node into db
     *
     * @param list
     * @return
     */
    public int batchInsertMerkleNode(List list);

    /**
     * update merkle node
     *
     * @param list
     * @return
     */
    public int batchUpdateMerkleNode(List list);

    /**
     * query merkle node by treeType, blockHeight, rootHash(if not null)
     *
     * @param merkleNodePO
     * @return
     */
    public MerkleNodePO queryMerkleNodeByIndex(MerkleNodePO merkleNodePO);

    /**
     * check the existence merkle node by treeType, level, rootHash(if not null)
     *
     * @param merkleNodePO
     * @return
     */
    public MerkleNodePO queryMerkleNodeByHash(MerkleNodePO merkleNodePO);

    /**
     * truncate table merkle_node and merkle_tree, just for test case
     *
     */
    public void trucateMerkleNode();
    public void trucateMerkleTree();

}
