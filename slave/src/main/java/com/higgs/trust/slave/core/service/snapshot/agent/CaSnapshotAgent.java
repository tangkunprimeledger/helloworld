package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.repository.config.ClusterConfigRepository;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.dao.po.ca.CaPO;
import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import com.higgs.trust.slave.dao.po.config.ClusterNodePO;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author WangQuanzhou
 * @desc ca snapshot agent
 * @date 2018/6/6 11:29
 */
@Slf4j @Component public class CaSnapshotAgent implements CacheLoader {

    @Autowired SnapshotService snapshot;
    @Autowired CaRepository caRepository;
    @Autowired ClusterConfigRepository clusterConfigRepository;
    @Autowired ClusterNodeRepository clusterNodeRepository;

    /**
     * get data from snapshot
     *
     * @param key
     * @param <T>
     * @return
     */
    private <T> T get(Object key) {
        return (T)snapshot.get(SnapshotBizKeyEnum.CA, key);
    }

    /**
     * insert  object into the snapshot
     *
     * @param key
     * @param value
     */
    private void insert(Object key, Object value) {
        snapshot.insert(SnapshotBizKeyEnum.CA, key, value);
    }

    /**
     * update  object into the snapshot
     *
     * @param key
     * @param value
     */
    private void update(Object key, Object value) {
        snapshot.update(SnapshotBizKeyEnum.CA, key, value);
    }

    /**
     * query CA
     *
     * @param user
     * @return
     */
    public Ca getCa(String user) {
        return get(new CaCacheKey(user));
    }

    /**
     * query ClusterConfig
     *
     * @param clusterName
     * @return
     */
    public ClusterConfig getClusterConfig(String clusterName) {
        return get(new CaCacheKey(clusterName));
    }

    /**
     * query nodeName
     *
     * @param nodeName
     * @return
     */
    public ClusterNode getClusterNode(String nodeName) {
        return get(new CaCacheKey(nodeName));
    }

    /**
     * save CA
     *
     * @param ca
     */
    public void saveCa(Ca ca) {
        insert(new CaSnapshotAgent.CaCacheKey(ca.getUser()), ca);
    }

    /**
     * update CA
     *
     * @param ca
     */
    public void updateCa(Ca ca) {
        update(new CaSnapshotAgent.CaCacheKey(ca.getUser()), ca);
    }

    /**
     * save ClusterConfig
     *
     * @param clusterConfig
     */
    public void saveClusterConfig(ClusterConfig clusterConfig) {
        insert(new CaSnapshotAgent.ClusterConfigCacheKey(clusterConfig.getClusterName()), clusterConfig);
    }

    /**
     * save ClusterConfig
     *
     * @param clusterConfig
     */
    public void updateClusterConfig(ClusterConfig clusterConfig) {
        update(new CaSnapshotAgent.CaCacheKey(clusterConfig.getClusterName()), clusterConfig);
    }

    /**
     * save ClusterNode
     *
     * @param clusterNode
     */
    public void saveClusterNode(ClusterNode clusterNode) {
        insert(new CaSnapshotAgent.ClusterConfigCacheKey(clusterNode.getNodeName()), clusterNode);
    }

    /**
     * save ClusterNode
     *
     * @param clusterNode
     */
    public void updateClusterNode(ClusterNode clusterNode) {
        update(new CaSnapshotAgent.CaCacheKey(clusterNode.getNodeName()), clusterNode);
    }

    /**
     * when cache is not exists,load from db
     */
    @Override public Object query(Object object) {
        if (object instanceof CaSnapshotAgent.CaCacheKey) {
            CaCacheKey key = (CaCacheKey)object;
            return caRepository.getCa(key.getUser());
        }
        if (object instanceof CaSnapshotAgent.ClusterConfigCacheKey) {
            ClusterConfigCacheKey key = (ClusterConfigCacheKey)object;
            return clusterConfigRepository.getClusterConfig(key.clusterName);
        }
        if (object instanceof CaSnapshotAgent.ClusterNodeCacheKey) {
            ClusterNodeCacheKey key = (ClusterNodeCacheKey)object;
            return clusterNodeRepository.getClusterNode(key.nodeName);
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
    @Override public boolean batchInsert(Map<Object, Object> insertMap) {
        if (insertMap == null || insertMap.isEmpty()) {
            log.info("[batchInsert]insertMap is empty");
            return true;
        }
        List<CaPO> caPOS = new LinkedList<>();
        List<ClusterConfigPO> clusterConfigPOS = new LinkedList<>();
        List<ClusterNodePO> clusterNodePOS = new LinkedList<>();
        for (Object key : insertMap.keySet()) {
            if (key instanceof CaSnapshotAgent.CaCacheKey) {
                caPOS.add((CaPO)insertMap.get(key));
            }
            if (key instanceof CaSnapshotAgent.ClusterConfigCacheKey) {
                clusterConfigPOS.add((ClusterConfigPO)insertMap.get(key));
            }
            if (key instanceof CaSnapshotAgent.ClusterNodeCacheKey) {
                clusterNodePOS.add((ClusterNodePO)insertMap.get(key));
            }
        }
        if (!CollectionUtils.isEmpty(caPOS)) {
            caRepository.batchInsert(caPOS);
        }
        if (!CollectionUtils.isEmpty(clusterConfigPOS)) {
            clusterConfigRepository.batchInsert(clusterConfigPOS);
        }
        if (!CollectionUtils.isEmpty(clusterNodePOS)) {
            clusterNodeRepository.batchInsert(clusterNodePOS);
        }
        return true;
    }

    /**
     * the method to batchUpdate data into db
     *
     * @param updateMap
     * @return
     */
    @Override public boolean batchUpdate(Map<Object, Object> updateMap) {
        if (updateMap == null || updateMap.isEmpty()) {
            log.info("[updateMap]updateMap is empty");
            return true;
        }
        List<CaPO> caPOS = new LinkedList<>();
        List<ClusterConfigPO> clusterConfigPOS = new LinkedList<>();
        List<ClusterNodePO> clusterNodePOS = new LinkedList<>();
        for (Object key : updateMap.keySet()) {
            if (key instanceof CaSnapshotAgent.CaCacheKey) {
                caPOS.add((CaPO)updateMap.get(key));
            }
            if (key instanceof CaSnapshotAgent.ClusterConfigCacheKey) {
                clusterConfigPOS.add((ClusterConfigPO)updateMap.get(key));
            }
            if (key instanceof CaSnapshotAgent.CaCacheKey) {
                clusterNodePOS.add((ClusterNodePO)updateMap.get(key));
            }
        }
        if (!CollectionUtils.isEmpty(caPOS)) {
            caRepository.batchUpdate(caPOS);
        }
        if (!CollectionUtils.isEmpty(clusterConfigPOS)) {
            clusterConfigRepository.batchUpdate(clusterConfigPOS);
        }
        if (!CollectionUtils.isEmpty(clusterNodePOS)) {
            clusterNodeRepository.batchUpdate(clusterNodePOS);
        }
        return true;
    }

    /**
     * the cache key of CA
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor public static class CaCacheKey extends BaseBO {
        private String user;
    }

    /**
     * the cache key of CA
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor public static class ClusterConfigCacheKey extends BaseBO {
        private String clusterName;
    }

    /**
     * the cache key of CA
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor public static class ClusterNodeCacheKey extends BaseBO {
        private String nodeName;
    }
}
