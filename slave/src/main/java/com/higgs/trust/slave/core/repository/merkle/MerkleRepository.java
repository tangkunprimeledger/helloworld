package com.higgs.trust.slave.core.repository.merkle;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.dao.merkle.MerkleDao;
import com.higgs.trust.slave.dao.po.merkle.MerkleNodePO;
import com.higgs.trust.slave.dao.po.merkle.MerkleTreePO;
import com.higgs.trust.slave.model.bo.merkle.MerkleNode;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author WangQuanzhou
 * @desc merkle repository
 * @date 2018/4/17 15:41
 */
@Repository @Slf4j public class MerkleRepository {

    private static final int INIT_CAPACITY = 300;

    @Autowired private MerkleDao merkleDao;

    /**
     * insert merkle tree into db
     *
     * @param merkleTree
     */
    public void insertMerkleTree(MerkleTree merkleTree) {
        // construct MerkleTreePO
        MerkleTreePO merkleTreePO =
            new MerkleTreePO(merkleTree.getRootHash(), merkleTree.getTotalLevel(), merkleTree.getMaxIndex(),
                merkleTree.getTreeType().getCode());
        merkleDao.insertMerkleTree(merkleTreePO);
    }

    /**
     * query merkle tree by treeType, blockHeight, rootHash(if not null)
     *
     * @param treeType
     * @return
     */
    public MerkleTree queryMerkleTree(String treeType) {
        MerkleTreePO merkleTreePO = new MerkleTreePO();
        merkleTreePO.setTreeType(treeType);
        merkleTreePO = merkleDao.queryMerkleTree(merkleTreePO);
        if (null == merkleTreePO) {
            return null;
        }
        MerkleTree merkleTree = new MerkleTree();
        BeanUtils.copyProperties(merkleTreePO, merkleTree);
        merkleTree.setTreeType(MerkleTypeEnum.getBizTypeEnumBycode(treeType));
        return merkleTree;
    }

    /**
     * insert merkle node into db
     *
     * @param list
     * @return
     */
    public int batchInsertMerkleNode(List list) {
        List<MerkleNodePO> addedList = new ArrayList(INIT_CAPACITY);
        Iterator<MerkleNode> iterator = list.iterator();
        while (iterator.hasNext()) {
            MerkleNode temp = iterator.next();
            addedList.add(
                new MerkleNodePO(temp.getNodeHash(), temp.getUuid(), temp.getIndex(), temp.getLevel(), temp.getParent(),
                    temp.getTreeType().getCode()));
        }
        return  merkleDao.batchInsertMerkleNode(addedList);
    }

    /**
     * update merkle node
     *
     * @param list
     */
    public int batchUpdateMerkleNode(List list) {
        List<MerkleNodePO> modifiedList = new ArrayList(INIT_CAPACITY);
        Iterator<MerkleNode> iterator = list.iterator();
        while (iterator.hasNext()) {
            MerkleNode temp = iterator.next();
            modifiedList.add(
                new MerkleNodePO(temp.getNodeHash(), temp.getUuid(), temp.getIndex(), temp.getLevel(), temp.getParent(),
                    temp.getTreeType().getCode()));
        }
        return  merkleDao.batchUpdateMerkleNode(modifiedList);
    }

    /**
     * query merkle node by treeType, blockHeight, rootHash(if not null)
     *
     * @param level
     * @param index
     * @param treeType
     * @return
     */
    public MerkleNode queryMerkleNodeByIndex(int level, long index, String treeType) {
        MerkleNodePO merkleNodePO = new MerkleNodePO();
        merkleNodePO.setIndex(index);
        merkleNodePO.setLevel(level);
        merkleNodePO.setTreeType(treeType);
        merkleNodePO = merkleDao.queryMerkleNodeByIndex(merkleNodePO);
        if (null == merkleNodePO) {
            return null;
        }
        MerkleNode merkleNode = new MerkleNode();
        BeanUtils.copyProperties(merkleNodePO, merkleNode);
        merkleNode.setTreeType(MerkleTypeEnum.getBizTypeEnumBycode(merkleNodePO.getTreeType()));
        return merkleNode;
    }

    /**
     * check the existence merkle node by treeType, level, rootHash(if not null)
     *
     * @param nodeHash
     * @param level
     * @param treeType
     * @return
     */
    public MerkleNode queryMerkleNodeByHash(String nodeHash, int level, String treeType) {
        MerkleNodePO merkleNodePO = new MerkleNodePO();
        merkleNodePO.setNodeHash(nodeHash);
        merkleNodePO.setLevel(level);
        merkleNodePO.setTreeType(treeType);
        merkleNodePO = merkleDao.queryMerkleNodeByHash(merkleNodePO);
        if (null == merkleNodePO) {
            return null;
        }
        MerkleNode merkleNode = new MerkleNode();
        BeanUtils.copyProperties(merkleNodePO, merkleNode);
        merkleNode.setTreeType(MerkleTypeEnum.getBizTypeEnumBycode(merkleNodePO.getTreeType()));
        return merkleNode;
    }
}
