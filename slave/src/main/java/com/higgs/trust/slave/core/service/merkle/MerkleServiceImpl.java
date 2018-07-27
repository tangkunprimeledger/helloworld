package com.higgs.trust.slave.core.service.merkle;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave.api.enums.MerkleStatusEnum;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.common.SnowflakeIdWorker;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.MerkleException;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.model.bo.merkle.MerkleNode;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WangQuanzhou
 * @desc merkle tree service
 * @date 2018/4/10 16:38
 */
@Service @Slf4j public class MerkleServiceImpl implements MerkleService {

    private static final int N = 2;

    private static final int INIT_CAPACITY = 300;

    @Autowired private SnowflakeIdWorker snowflakeIdWorker;

    /**
     * create a merkle tree
     *
     * @param type
     * @param dataList
     * @return
     */
    @Override public MerkleTree build(MerkleTypeEnum type, List<Object> dataList) {
        Profiler.enter("[MerkleServiceImpl.build.monitor]");
        try {
            // validate param
            if (CollectionUtils.isEmpty(dataList) || null == type) {
                log.error("[build] param validate failed");
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_PARAM_NOT_VALID_EXCEPTION,
                    "[build] param validate failed");
            }

            if (log.isDebugEnabled()) {
                log.debug("[build] start to build merkle tree, treeType={}, dataList size={}", type.getCode(),
                    dataList.size());
            }

            // merkleTree
            MerkleTree merkleTree = new MerkleTree();
            // merkleNode hashMap, the key is (level,index)
            Map<String, MerkleNode> nodeMap = new ConcurrentHashMap(INIT_CAPACITY);
            // tempList
            List<String> tempList = new LinkedList();
            // current level, start with 1
            int level = 1;

            // acquire the hash of the given Object list
            Profiler.enter("[acquire the hash of the given Object list]");
            List<String> leafHashList = leafHash(dataList);
            Profiler.release();
            // special handle level one
            Profiler.enter("[add merkleNode into nodeMap for level one]");
            addToNodeMap(leafHashList, level, type, nodeMap);
            Profiler.release();
            merkleTree.setTreeType(type);
            merkleTree.setMaxIndex((long)leafHashList.size() - 1L);
            if (log.isDebugEnabled()) {
                log.debug("total merkleNode count of level {} is {}", level, leafHashList.size());
            }

            // handle the other level
            while (leafHashList.size() != 1) {
                Profiler.enter("[calculate node hash and add merkleNode into nodeMap]");
                tempList = getNewHashList(leafHashList);
                level++;
                if (log.isDebugEnabled()) {
                    log.debug("total merkleNode count of level {} is {}", level, tempList.size());
                }
                addToNodeMap(tempList, level, type, nodeMap);
                // update parent of sublevel
                for (int j = 0; j < leafHashList.size(); j++) {
                    nodeMap.get(getKey(level - 1, j)).setParent(nodeMap.get(getKey(level, j / N)).getUuid());
                }
                leafHashList = tempList;
                Profiler.release();
            }

            // update merkleTree's debugrmation
            merkleTree.setRootHash(leafHashList.get(0));
            merkleTree.setTotalLevel(level);
            merkleTree.setNodeMap(nodeMap);
            if (log.isDebugEnabled()) {
                log.debug("[build] end build merkle tree, treeType={}, dataList size={}", type.getCode(),
                    dataList.size());
            }

            return merkleTree;
        } finally {
            Profiler.release();
        }
    }

    @Override public boolean isExist(MerkleTree merkleTree, Object obj) {
        String objHash = getSHA2HexValue(JSON.toJSONString(obj));

        MerkleNode merkleNode = getMerkleNodeByHash(merkleTree, objHash);

        return merkleNode != null;
    }

    /**
     * update a merkle tree
     *
     * @param merkleTree
     * @param objOld
     * @param objNew
     */
    @Override public void update(MerkleTree merkleTree, Object objOld, Object objNew) {
        Profiler.enter("[MerkleServiceImpl.update.monitor]");

        try {
            // validate param
            if (null == merkleTree || null == objOld || null == objNew || null == merkleTree.getTreeType()
                || null == merkleTree.getNodeMap()) {
                log.error("[update] param is null");
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_PARAM_NOT_VALID_EXCEPTION,
                    "[update] param is null");
            }

            MerkleNode merkleNode = null;
            String type = merkleTree.getTreeType().getCode();
            String oldHash = getSHA2HexValue(JSON.toJSONString(objOld));
            String newHash = getSHA2HexValue(JSON.toJSONString(objNew));
            long leafIndex = -1L;

            if (log.isDebugEnabled()) {
                log.debug("[update] start to udpate merkleTree, type={}, oldHash={}, objOld={}, newHash={}, objNew={}",
                    type, oldHash, JSON.toJSONString(objOld), newHash, JSON.toJSONString(objNew));
            }

            // acquire nodeMap stored in merkleTree
            Map<String, MerkleNode> nodeMap = merkleTree.getNodeMap();
            // check existence of merkleNode with oldHash, it must be in nodeMap before update
            Profiler.enter("[get merkleNode by oldHash]");
            merkleNode = getMerkleNodeByHash(merkleTree, oldHash);
            Profiler.release();
            if (null == merkleNode) {
                log.error(
                    "[update] update merkleTree error, hash(objOld) doesn't exist in merkleTree, hash(objOld)={}, type={},merkleRootHash={}",
                    oldHash, type, merkleTree.getRootHash());
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_NODE_UPDATE_EXCEPTION,
                    "[update] update merkleTree error, hash(objOld) doesn't exist in merkleTree");
            }
            // check existence of merkleNode with newHash, it must not be in nodeMap before update
            Profiler.enter("[get merkleNode by newHash]");
            MerkleNode tempNode = getMerkleNodeByHash(merkleTree, newHash);
            Profiler.release();
            if (null != tempNode) {
                log.error(
                    "[update] update merkleTree error, hash(objNew) already exist in merkleTree, hash(objNew)={}, type={},merkleRootHash={}",
                    newHash, type, merkleTree.getRootHash());
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_NODE_UPDATE_EXCEPTION,
                    "[update] update merkleTree error, hash(objNew) already exist in merkleTree");
            }

            leafIndex = merkleNode.getIndex();
            // leafIndex must not be greater than maxIndex
            long maxIndex = merkleTree.getMaxIndex();
            if (leafIndex > maxIndex) {
                log.error("[update] calculate index error, type={},level={},leafIndex={},maxIndex={}", type, 1,
                    leafIndex, maxIndex);
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_CALCULATE_INDEX_EXCEPTION,
                    "[update] calculate index error");
            }

            // update the merkleNode hash of leaf level and modify to nodeMap
            merkleNode.setNodeHash(newHash);
            if (MerkleStatusEnum.NO_CHANGE == merkleNode.getStatus()) {
                merkleNode.setStatus(MerkleStatusEnum.MODIFY);
            }
            nodeMap.put(getKey(1, leafIndex), merkleNode);

            int totalLevel = merkleTree.getTotalLevel();

            // batch query merkleNode from nodeMap or db
            //            getMerkleNodeListForUpdate(merkleTree, totalLevel, leafIndex);

            for (int i = 1; i < totalLevel; i++) {
                Profiler.enter("[update parentNode]");
                if (log.isDebugEnabled()) {
                    log.debug("[update] start to update parentNode, leafIndex={}, maxIndex={}, currentLevel={}",
                        leafIndex, maxIndex, i);
                }
                updateParent(merkleTree, i, leafIndex, maxIndex);
                // refresh the index
                leafIndex = leafIndex / N;
                maxIndex = maxIndex / N;
                Profiler.release();
            }
            merkleTree.setRootHash(nodeMap.get(getKey(totalLevel, leafIndex)).getNodeHash());
            if (log.isDebugEnabled()) {
                log.debug("[update] end udpate merkleTree, type={}, new rootHash={}", type, merkleTree.getRootHash());
            }

        } finally {
            Profiler.release();
        }
    }

    /**
     * add one node into a merkle tree
     *
     * @param merkleTree
     * @param obj
     */
    @Override public void add(MerkleTree merkleTree, Object obj) {
        Profiler.enter("[MerkleServiceImpl.add.monitor]");
        try {
            // validate param
            if (null == merkleTree || null == obj || null == merkleTree.getTreeType() || null == merkleTree
                .getNodeMap()) {
                log.error("[add] param is null");
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_PARAM_NOT_VALID_EXCEPTION, "[add] param is null");
            }

            Map<String, MerkleNode> nodeMap = merkleTree.getNodeMap();
            String hash = getSHA2HexValue(JSON.toJSONString(obj));
            String type = merkleTree.getTreeType().getCode();
            if (log.isDebugEnabled()) {
                log.debug("[add] start to add hash(obj) to merkleTree, hash(obj)={}, obj={},type={}", hash,
                    JSON.toJSONString(obj), type);
            }

            // check existence of merkleNode with exact nodeHash, it must not be in nodeMap before add
            Profiler.enter("[get merkleNode by hash]");
            MerkleNode merkleNode = getMerkleNodeByHash(merkleTree, hash);
            Profiler.release();
            if (null != merkleNode) {
                log.error(
                    "[add] add merkleTree error, hash(obj) already exist in merkleTree, type={},merkleRootHash={}",
                    type, merkleTree.getRootHash());
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_ALREADY_EXIST_EXCEPTION,
                    "[add] add merkleTree error, hash(obj) already exist in merkleTree");
            }

            int totalLevel = merkleTree.getTotalLevel();
            long maxIndex = merkleTree.getMaxIndex();
            long leafIndex = maxIndex + 1L;

            // if (maxIndex+1) = 2^n, then totalLevel =totalLevel+1, note maxIndex starts from zero
            if (0 == ((maxIndex + 1L) & (maxIndex))) {
                if (log.isDebugEnabled()) {
                    log.debug("[add] (maxIndex+1)={},is 2^n", maxIndex + 1L);
                }
                totalLevel++;
                // construct a new rootNode and put it into top level of MerkleTree
                MerkleNode rootNode =
                    new MerkleNode(null, String.valueOf(snowflakeIdWorker.nextId()), 0L, totalLevel, null,
                        merkleTree.getTreeType(), MerkleStatusEnum.ADD);
                // set old rootNode's  parent to new rootNode's uuid
                MerkleNode temp = getMerkleNodeByIndex(nodeMap, getKey(merkleTree.getTotalLevel(), 0L),
                    merkleTree.getTreeType().getCode());
                temp.setParent(rootNode.getUuid());
                if (MerkleStatusEnum.NO_CHANGE == temp.getStatus()) {
                    temp.setStatus(MerkleStatusEnum.MODIFY);
                }
                // update merkleTree's totalLevel
                merkleTree.setTotalLevel(totalLevel);
                // put new rootNode into nodeMap
                nodeMap.put(getKey(totalLevel, 0L), rootNode);
            }

            // construct leafNode and put it into leafLevel tail
            MerkleNode leafNode = new MerkleNode(hash, String.valueOf(snowflakeIdWorker.nextId()), leafIndex, 1, null,
                merkleTree.getTreeType(), MerkleStatusEnum.ADD);
            nodeMap.put(getKey(1, leafIndex), leafNode);
            merkleTree.setMaxIndex(leafIndex);

            // batch query merkleNode from nodeMap or db
            //            getMerkleNodeListForAdd(merkleTree, totalLevel, leafIndex);

            // handle the other level
            for (int i = 1; i < totalLevel; i++) {
                Profiler.enter("[update or add parentNode]");
                updateAddParent(merkleTree, i, leafIndex, maxIndex);
                leafIndex = leafIndex / N;
                maxIndex = maxIndex / N;
                Profiler.release();
            }
            merkleTree.setRootHash(nodeMap.get(getKey(totalLevel, leafIndex)).getNodeHash());
            if (log.isDebugEnabled()) {
                log.debug("[add] end add hash(obj) to merkleTree, hash(obj)={},type={}, new rootHash={}", hash, type,
                    merkleTree.getRootHash());
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * query a merkle tree with the exact type
     *
     * @param treeType
     * @return
     */
    @Override public MerkleTree queryMerkleTree(MerkleTypeEnum treeType) {
        // validate param
        if (null == treeType) {
            log.error("[queryMerkleTree] treeType is null");
            throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_PARAM_NOT_VALID_EXCEPTION,
                "[queryMerkleTree] treeType is null");
        }
        if (log.isDebugEnabled()) {
            log.debug("[queryMerkleTree] start to queryMerkleTree, type={}", treeType);
        }

        return null;
    }

    /**
     * get new hashList based on tempList
     *
     * @param tempList
     * @return
     */
    private List getNewHashList(List tempList) {
        if (CollectionUtils.isEmpty(tempList)) {
            return null;
        }
        List<String> newObjList = new LinkedList<>();
        int index = 0;
        while (index < tempList.size()) {
            String temp = "";
            for (int i = index; i < index + N && i < tempList.size(); i++) {
                if (i != tempList.size()) {
                    temp = temp + tempList.get(i);
                }
            }
            // sha2 hex value
            String sha2HexValue = getSHA2HexValue(temp);
            newObjList.add(sha2HexValue);

            index += N;
        }
        return newObjList;
    }

    /**
     * Return hex string
     *
     * @param str
     * @return
     */
    private String getSHA2HexValue(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        byte[] cipherByte;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes());
            cipherByte = md.digest();
            return Hex.encodeHexString(cipherByte);
        } catch (Throwable e) {
            log.error("[getSHA2HexValue] calculate hash error, str={}", str, e);
            throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_CALCULATE_HASH_EXCEPTION,
                "[getSHA2HexValue] calculate hash error");
        }
    }

    /**
     * calculate the hash of the given Object list
     *
     * @param list
     * @return List
     */
    private List<String> leafHash(List<Object> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List leafHashList = new LinkedList();
        Set leafHashSet = new HashSet(INIT_CAPACITY);
        for (Object temp : list) {
            String tempHash = getSHA2HexValue(JSON.toJSONString(temp));
            if (!leafHashSet.add(tempHash)) {
                log.error("[leafHash] dataList contains duplicate object,obj ={}", JSON.toJSONString(temp));
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_NODE_BUILD_DUPLICATE_EXCEPTION,
                    "[leafHash] dataList contains duplicate object");
            }
            leafHashList.add(tempHash);
        }
        return leafHashList;
    }

    /**
     * construct a key for the nodeMap
     *
     * @param level
     * @param index
     * @return
     */
    private String getKey(int level, long index) {
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder.append(level).append(",").append(index);
        return stringBuilder.toString();
    }

    /**
     * parse key to level and index
     *
     * @param key
     * @return
     */
    private String[] parseKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        // the format of levelIndex is like level,index, the separator is ,
        String[] levelIndex = key.split(",");
        return levelIndex;
    }

    /**
     * construct merkleNode and add merkleNode into nodeMap
     *
     * @param tempList
     * @param level
     * @param type
     * @param nodeMap
     */
    private void addToNodeMap(List<String> tempList, int level, MerkleTypeEnum type, Map nodeMap) {
        for (int i = 0; i < tempList.size(); i++) {
            MerkleNode merkleNode =
                new MerkleNode(tempList.get(i), String.valueOf(snowflakeIdWorker.nextId()), i, level, null, type,
                    MerkleStatusEnum.ADD);
            // add a merkle node into nodeMap
            nodeMap.put(getKey(level, i), merkleNode);
            log.debug("[addToNodeMap] success add merkleNode into nodeMap, nodeHsh={},level={},index={},type={}",
                tempList.get(i), level, i, type.getCode());
        }
    }

    /**
     * get merkleNode
     *
     * @param nodeMap
     * @param key
     * @param type
     * @return
     */
    private MerkleNode getMerkleNodeByIndex(Map<String, MerkleNode> nodeMap, String key, String type) {
        MerkleNode merkleNode = null;
        if (null != nodeMap) {
            merkleNode = nodeMap.get(key);
        }

        if (log.isDebugEnabled()) {
            log.debug("[getMerkleNodeByIndex] merkleNode={}", JSON.toJSONString(merkleNode));
        }
        return merkleNode;
    }

    /**
     * update parent node hash
     *
     * @param merkleTree
     * @param level
     * @param leafIndex
     * @param maxIndex
     */
    private void updateParent(MerkleTree merkleTree, int level, long leafIndex, long maxIndex) {
        MerkleNode left = null;
        MerkleNode right = null;
        MerkleNode parent = null;
        Map<String, MerkleNode> nodeMap = merkleTree.getNodeMap();
        String type = merkleTree.getTreeType().getCode();
        if (log.isDebugEnabled()) {
            log.debug("[updateParent] level={}, leafIndex={}, maxIndex={}, type={}", level, leafIndex, maxIndex, type);
        }

        // this means leafIndex is lower than max index of the currrent level and (leafIndex+1) is even
        if (leafIndex <= maxIndex && 1 == (leafIndex % N)) {
            if (log.isDebugEnabled()) {
                log.debug("[updateParent] leafIndex <= maxIndex && 1 == (leafIndex % N)");
            }
            left = getMerkleNodeByIndex(nodeMap, getKey(level, leafIndex - 1), type);
            right = getMerkleNodeByIndex(nodeMap, getKey(level, leafIndex), type);
            parent = getMerkleNodeByIndex(nodeMap, getKey(level + 1, leafIndex / N), type);
            if (!left.getParent().equals(parent.getUuid()) || !right.getParent().equals(parent.getUuid())) {
                log.error(
                    "[updateParent] update parent merkle node error, left node's parent={},right node's parent={},parent node's={}",
                    left.getParent(), right.getParent(), parent.getUuid());
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_UPDATE_PARENT_EXCEPTION,
                    "[updateParent] update parent merkle node error");
            }
            parent.setNodeHash(getSHA2HexValue(left.getNodeHash() + right.getNodeHash()));
            if (MerkleStatusEnum.NO_CHANGE == parent.getStatus()) {
                parent.setStatus(MerkleStatusEnum.MODIFY);
            }
            return;
        }

        // this means leafIndex is the max index of the currrent level and (leafIndex+1) is odd
        if (leafIndex == maxIndex && 0 == (leafIndex % N)) {
            if (log.isDebugEnabled()) {
                log.debug("[updateParent] leafIndex == maxIndex && 0 == (leafIndex % N)");
            }
            left = getMerkleNodeByIndex(nodeMap, getKey(level, leafIndex), type);
            parent = getMerkleNodeByIndex(nodeMap, getKey(level + 1, leafIndex / N), type);
            if (!left.getParent().equals(parent.getUuid())) {
                log.error("[updateParent] update parent merkle node error, left node's parent={},parent node's={}",
                    left.getParent(), parent.getUuid());
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_UPDATE_PARENT_EXCEPTION,
                    "[updateParent] update parent merkle node error");
            }
            parent.setNodeHash(getSHA2HexValue(left.getNodeHash()));
            if (MerkleStatusEnum.NO_CHANGE == parent.getStatus()) {
                parent.setStatus(MerkleStatusEnum.MODIFY);
            }
            return;
        }

        // this means leafIndex is not the max index of the currrent level and (leafIndex+1) is odd
        if (leafIndex < maxIndex && 0 == (leafIndex % N)) {
            if (log.isDebugEnabled()) {
                log.debug("[updateParent] leafIndex < maxIndex && 0 == (leafIndex % N)");
            }
            left = getMerkleNodeByIndex(nodeMap, getKey(level, leafIndex), type);
            right = getMerkleNodeByIndex(nodeMap, getKey(level, leafIndex + 1), type);
            parent = getMerkleNodeByIndex(nodeMap, getKey(level + 1, leafIndex / N), type);
            if (!left.getParent().equals(parent.getUuid()) || !right.getParent().equals(parent.getUuid())) {
                log.error(
                    "[updateParent] update parent merkle node error, left node's parent={},right node's parent={},parent node's={}",
                    left.getParent(), right.getParent(), parent.getUuid());
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_UPDATE_PARENT_EXCEPTION,
                    "[updateParent] update parent merkle node error");
            }
            parent.setNodeHash(getSHA2HexValue(left.getNodeHash() + right.getNodeHash()));
            if (MerkleStatusEnum.NO_CHANGE == parent.getStatus()) {
                parent.setStatus(MerkleStatusEnum.MODIFY);
            }
        }
    }

    /**
     * query a node with exact nodeHash and type
     *
     * @param merkleTree
     * @param nodeHash
     * @return
     */
    private MerkleNode getMerkleNodeByHash(MerkleTree merkleTree, String nodeHash) {
        MerkleNode merkleNode = null;
        Map<String, MerkleNode> nodeMap = merkleTree.getNodeMap();
        String type = merkleTree.getTreeType().getCode();
        if (log.isDebugEnabled()) {
            log.debug("[getMerkleNodeByHash] nodeHash={},type={}", nodeHash, type);
        }

        Long leafIndex = -1L;
        if (!CollectionUtils.isEmpty(nodeMap)) {
            // loop through nodeMap to find merkleNode with exact nodeHash
            for (Map.Entry<String, MerkleNode> entry : nodeMap.entrySet()) {
                // if hash(objOld) exist in nodeMap and level = 1
                if (nodeHash.equals(entry.getValue().getNodeHash())
                    && Integer.valueOf(parseKey(entry.getKey())[0]) == 1) {
                    merkleNode = nodeMap.get(entry.getKey());
                    leafIndex = merkleNode.getIndex();
                    break;
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("[getMerkleNodeByHash] merkleNode={}", JSON.toJSONString(merkleNode));
        }
        return merkleNode;
    }

    /**
     * update or add one merkleNode into merkleTree
     *
     * @param merkleTree
     * @param level
     * @param leafIndex
     * @param maxIndex
     */
    private void updateAddParent(MerkleTree merkleTree, int level, long leafIndex, long maxIndex) {
        MerkleNode left = null;
        MerkleNode right = null;
        MerkleNode parent = null;
        Map nodeMap = merkleTree.getNodeMap();
        String type = merkleTree.getTreeType().getCode();
        if (log.isDebugEnabled()) {
            log.debug("[add] leafIndex={}, maxIndex={}, type={}, currentLevel={}", leafIndex, maxIndex, type, level);
        }

        if (0 == leafIndex % N) {
            // this means a parent should be added into nodeMap
            if ((leafIndex / N) > (maxIndex / N)) {
                if (log.isDebugEnabled()) {
                    log.debug("[add] (leafIndex / N) > (maxIndex / N), add a parent node to merkleTree");
                }
                left = getMerkleNodeByIndex(nodeMap, getKey(level, leafIndex), type);
                parent = new MerkleNode(getSHA2HexValue(left.getNodeHash()), String.valueOf(snowflakeIdWorker.nextId()),
                    leafIndex / N, level + 1, null, merkleTree.getTreeType(), MerkleStatusEnum.ADD);
                left.setParent(parent.getUuid());
                nodeMap.put(getKey(level + 1, leafIndex / N), parent);
                return;
            }
            // this means only have left chaild
            left = getMerkleNodeByIndex(nodeMap, getKey(level, leafIndex), type);
            parent = getMerkleNodeByIndex(nodeMap, getKey(level + 1, leafIndex / N), type);
            if (!left.getParent().equals(parent.getUuid())) {
                log.error("[add] update parent error, left's parent={}, parent's uuid={}", left.getParent(),
                    parent.getUuid());
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_UPDATE_PARENT_EXCEPTION,
                    "[add] update parent error");
            }
            parent.setNodeHash(getSHA2HexValue(left.getNodeHash()));
            if (parent.getStatus() == MerkleStatusEnum.NO_CHANGE) {
                parent.setStatus(MerkleStatusEnum.MODIFY);
            }
            return;
        }
        if (1 == leafIndex % N) {
            if (log.isDebugEnabled()) {
                log.debug("[add] 1 == leafIndex % N, update a parent node");
            }
            left = getMerkleNodeByIndex(nodeMap, getKey(level, leafIndex - 1L), type);
            right = getMerkleNodeByIndex(nodeMap, getKey(level, leafIndex), type);
            parent = getMerkleNodeByIndex(nodeMap, getKey(level + 1, leafIndex / N), type);
            if (!left.getParent().equals(parent.getUuid())) {
                log.error("[add] update parent error, left's parent={}, parent's uuid={}", left.getParent(),
                    parent.getUuid());
                throw new MerkleException(SlaveErrorEnum.SLAVE_MERKLE_UPDATE_PARENT_EXCEPTION,
                    "[add] update parent error");
            }
            parent.setNodeHash(getSHA2HexValue(left.getNodeHash() + right.getNodeHash()));
            if (parent.getStatus() == MerkleStatusEnum.NO_CHANGE) {
                parent.setStatus(MerkleStatusEnum.MODIFY);
            }
            right.setParent(parent.getUuid());
        }
    }

}
