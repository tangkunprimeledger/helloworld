package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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

    //TODO You  should provide insert and update method for yourself to use by using snapshot insert or uodate method .
    private void put(Object key, Object object) {
        //snapshot.put(SnapshotBizKeyEnum.MERKLE_TREE, key, object);
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
     * build an new merkle tree for obj and save to snapshot
     *
     * @param typeEnum
     * @param datas
     * @return
     */
    public MerkleTree buildMerleTree(MerkleTypeEnum typeEnum, Object[] datas) {
        MerkleTree merkleTree = merkleService.build(typeEnum, Arrays.asList(datas));
        put(new MerkleTreeCacheKey(typeEnum), merkleTree);
        return merkleTree;
    }

    /**
     * append an new obj to mekler tree and update snapshot
     *
     * @param merkleTree
     * @param _new
     */
    public void appendChild(MerkleTree merkleTree, Object _new) {
        merkleService.add(merkleTree, _new);
        put(new MerkleTreeCacheKey(merkleTree.getTreeType()), merkleTree);
    }

    /**
     * modify the obj in the merkle tree
     *
     * @param merkleTree
     * @param _old
     * @param _new
     */
    public void modifyMerkleTree(MerkleTree merkleTree, Object _old, Object _new) {
        merkleService.update(merkleTree, _old, _new);
        put(new MerkleTreeCacheKey(merkleTree.getTreeType()), merkleTree);
    }

    /**
     * when cache is not exists,load from db
     */
    @Override
    public Object query(Object object) {
        if (object instanceof MerkleTreeCacheKey) {
            MerkleTreeCacheKey key = (MerkleTreeCacheKey) object;
            return merkleService.queryMerkleTree(key.getMerkleTypeEnum());
        }
        log.error("not found load function for cache key:{}", object);
        return null;
    }

    /**
     * the method to batchInsert data into db
     *
     * @param insertMap
     * @return
     */
    //TODO to implements your own bachInsert method for db
    @Override
    public boolean batchInsert(Map<Object, Object> insertMap) {
        return false;
    }

    /**
     * the method to batchUpdate data into db
     *
     * @param updateMap
     * @return
     */
    //TODO to implements your own bachUpdate method for db
    @Override
    public boolean batchUpdate(Map<Object, Object> updateMap) {
        return false;
    }

    /**
     * the cache key of merkle tree
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MerkleTreeCacheKey extends BaseBO {
        private MerkleTypeEnum merkleTypeEnum;
    }
}
