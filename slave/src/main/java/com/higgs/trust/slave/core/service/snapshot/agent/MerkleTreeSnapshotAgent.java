package com.higgs.trust.slave.core.service.snapshot.agent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuyu
 * @description an agent for merkle tree snapshot
 * @date 2018-04-11
 */
@Slf4j
@Component
public class MerkleTreeSnapshotAgent implements CacheLoader {
    @Autowired
    SnapshotService snapshot;
    @Autowired
    MerkleService merkleService;

    private <T> T get(Object key) {
        return (T) snapshot.get(SnapshotBizKeyEnum.MERKLE_TREE, key);
    }

    private void insert(Object key, Object object) {
        snapshot.insert(SnapshotBizKeyEnum.MERKLE_TREE, key, object);
    }
    private void update(Object key, Object object) {
        snapshot.update(SnapshotBizKeyEnum.MERKLE_TREE, key, object);
    }
    /**
     * get merkle tree from snapshot,if is not exist,should build
     *
     * @param typeEnum
     * @return
     */
    public MerkleTree getMerkleTree(MerkleTypeEnum typeEnum) {
        return get(new MerkleTreeCacheKey(typeEnum));
    }

    /**
     * is exist node
     *
     * @param typeEnum
     * @param node
     * @return
     */
    public boolean isExist(MerkleTypeEnum typeEnum, MerkleDataNode node) {
        NodesCacheKey nodesCacheKey = new NodesCacheKey(typeEnum);
        Map<String, MerkleDataNode> nodes = get(nodesCacheKey);
        if (nodes == null) {
            return false;
        }
        boolean result = nodes.containsKey(node.getUniqKey());

        return result;
    }

    /**
     * add node to tmp node list
     *
     * @param merkleType
     * @param _new
     * @return
     */
    public void addNode(MerkleTypeEnum merkleType, MerkleDataNode _new) {
        NodesCacheKey nodesCacheKey = new NodesCacheKey(merkleType);
        Map<String, MerkleDataNode> nodes = get(nodesCacheKey);
        if (nodes == null) {
            nodes = Maps.newLinkedHashMap();
            insert(nodesCacheKey, nodes);
        }
        nodes.put(_new.getUniqKey(), _new);
    }

    /**
     * update the obj in the merkle tree
     *
     * @param merkleType
     * @param _old
     * @param _new
     */
    public void updateNode(MerkleTypeEnum merkleType, MerkleDataNode _old, MerkleDataNode _new) {
        Map<String, Object> nodes = get(new NodesCacheKey(merkleType));
        if (nodes == null) {
            log.error("merkle update but nodes is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (!nodes.containsKey(_old.getUniqKey())) {
            log.error("merkle update but old value is not exist");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        nodes.put(_new.getUniqKey(), _new);
    }


    /**
     * build an new merkle tree for obj and save to snapshot
     *
     * @param typeEnum
     * @return
     */
    public MerkleTree buildMerkleTree(MerkleTypeEnum typeEnum) {
        LinkedHashMap<String, MerkleDataNode> nodes = get(new NodesCacheKey(typeEnum));
        if (MapUtils.isEmpty(nodes)) {
            return null;
        }
        MerkleTree merkleTree = merkleService.build(typeEnum, Lists.newLinkedList(nodes.values()));
        return merkleTree;
    }

    /**
     * when cache is not exists,load from db
     */
    @Override
    public Object query(Object object) {
        return null;
    }

    /**
     * the method to batchInsert data into db
     *
     * @param insertList
     * @return
     */
    @Override
    public boolean batchInsert(List<Pair<Object, Object>> insertList) {
        return true;
    }

    /**
     * the method to batchUpdate data into db
     *
     * @param updateList
     * @return
     */
    @Override
    public boolean batchUpdate(List<Pair<Object, Object>> updateList) {
        return true;
    }


    /**
     * cache node
     */
    public interface MerkleDataNode {
        String getUniqKey();
    }

    /**
     * the cache key of merkle tree
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor public static class MerkleTreeCacheKey extends BaseBO {
        private MerkleTypeEnum merkleTypeEnum;
    }
    /**
     * the cache node list of merkle tree
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor public static class NodesCacheKey extends BaseBO {
        private MerkleTypeEnum merkleTypeEnum;
    }
}
