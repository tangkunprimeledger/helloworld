package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import com.higgs.trust.slave.model.bo.snapshot.CacheKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author liuyu
 * @description an agent for merkle tree snapshot
 * @date 2018-04-11
 */
@Slf4j @Component public class MerkleTreeSnapshotAgent implements CacheLoader {
    @Autowired SnapshotService snapshot;
    @Autowired MerkleService merkleService;

    private <T> T get(Object key) {
        return (T)snapshot.get(SnapshotBizKeyEnum.MERKLE_TREE, key);
    }
    private void put(Object key, Object object) {
        snapshot.put(SnapshotBizKeyEnum.MERKLE_TREE, key, object);
    }

    /**
     * get merkle tree from snapshot,if is not exist,should build
     *
     * @param typeEnum
     * @return
     */
    public MerkleTree getMerkleTree(MerkleTypeEnum typeEnum) {
        return get(new MerkleTreeChachKey(typeEnum));
    }

    /**
     * build an new merkle tree for obj and save to snapshot
     *
     * @param typeEnum
     * @param datas
     * @return
     */
    public MerkleTree buildMerleTree(MerkleTypeEnum typeEnum,Object[] datas){
        MerkleTree merkleTree = merkleService.build(typeEnum, Arrays.asList(datas));
        put(new MerkleTreeChachKey(typeEnum),merkleTree);
        return merkleTree;
    }

    /**
     * append an new obj to mekler tree and update snapshot
     *
     * @param merkleTree
     * @param _new
     */
    public void appendChild(MerkleTree merkleTree,Object _new){
        merkleService.add(merkleTree,_new);
        put(new MerkleTreeChachKey(merkleTree.getTreeType()),merkleTree);
    }

    /**
     * modify the obj in the merkle tree
     *
     * @param merkleTree
     * @param _old
     * @param _new
     */
    public void modifyMerkleTree(MerkleTree merkleTree,Object _old,Object _new){
        merkleService.update(merkleTree,_old,_new);
        put(new MerkleTreeChachKey(merkleTree.getTreeType()),merkleTree);
    }

    /**
     * when cache is not exists,load from db
     */
    @Override public Object query(Object object) {
        if (object instanceof MerkleTreeChachKey) {
            MerkleTreeChachKey key = (MerkleTreeChachKey)object;
            return merkleService.queryMerkleTree(key.getMerkleTypeEnum());
        }
        log.error("not found load function for cache key:{}", object);
        return null;
    }

    /**
     * the cache key of merkle tree
     */
    @Getter @Setter @AllArgsConstructor public class MerkleTreeChachKey extends CacheKey {
        private MerkleTypeEnum merkleTypeEnum;
    }
}
