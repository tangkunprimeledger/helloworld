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
import com.higgs.trust.slave.model.enums.UsageEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

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
     * @param user
     * @param usage
     * @return
     * @desc query CA
     */
    public CaPO getCa(String user, String usage) {
        return get(new CaCacheKey(user, usage));
    }

    /**
     * query ClusterConfig
     *
     * @param clusterName
     * @return
     */
    public ClusterConfigPO getClusterConfig(String clusterName) {
        return get(new ClusterConfigCacheKey(clusterName));
    }

    /**
     * query nodeName
     *
     * @param nodeName
     * @return
     */
    public ClusterNodePO getClusterNode(String nodeName) {
        return get(new ClusterNodeCacheKey(nodeName));
    }

    /**
     * save CA
     *
     * @param ca
     */
    public void saveCa(Ca ca) {
        CaPO caPO = new CaPO();
        BeanUtils.copyProperties(ca, caPO);
        insert(new CaCacheKey(ca.getUser(), ca.getUsage()), caPO);
    }

    /**
     * update CA
     *
     * @param ca
     */
    public void updateCa(Ca ca) {
        CaPO caPO = new CaPO();
        BeanUtils.copyProperties(ca, caPO);
        update(new CaCacheKey(caPO.getUser(), caPO.getUsage()), caPO);
    }

    /**
     * save ClusterConfig
     *
     * @param clusterConfig
     */
    public void saveClusterConfig(ClusterConfig clusterConfig) {
        ClusterConfigPO clusterConfigPO = new ClusterConfigPO();
        BeanUtils.copyProperties(clusterConfig, clusterConfigPO);
        insert(new ClusterConfigCacheKey(clusterConfig.getClusterName()), clusterConfigPO);
    }

    /**
     * save ClusterConfig
     *
     * @param clusterConfig
     */
    public void updateClusterConfig(ClusterConfig clusterConfig) {
        ClusterConfigPO clusterConfigPO = new ClusterConfigPO();
        BeanUtils.copyProperties(clusterConfig, clusterConfigPO);
        update(new ClusterConfigCacheKey(clusterConfigPO.getClusterName()), clusterConfigPO);
    }

    /**
     * save ClusterNode
     *
     * @param clusterNode
     */
    public void saveClusterNode(ClusterNode clusterNode) {
        ClusterNodePO clusterNodePO = new ClusterNodePO();
        BeanUtils.copyProperties(clusterNode, clusterNodePO);
        insert(new ClusterNodeCacheKey(clusterNode.getNodeName()), clusterNodePO);
    }

    /**
     * save ClusterNode
     *
     * @param clusterNode
     */
    public void updateClusterNode(ClusterNode clusterNode) {
        ClusterNodePO clusterNodePO = new ClusterNodePO();
        BeanUtils.copyProperties(clusterNode, clusterNodePO);
        update(new ClusterNodeCacheKey(clusterNodePO.getNodeName()), clusterNodePO);
    }

    /**
     * when cache is not exists,load from db
     */
    @Override public Object query(Object object) {
        if (object instanceof CaSnapshotAgent.CaCacheKey) {
            CaCacheKey key = (CaCacheKey)object;
            Ca ca = null;
            if (UsageEnum.BIZ.getCode().equals(key.getUsage())) {
                ca = caRepository.getCaForBiz(key.getUser());
            }
            if (UsageEnum.CONSENSUS.getCode().equals(key.getUsage())) {
                ca = caRepository.getCaForConsensus(key.getUser());
            }
            if (null == ca) {
                return null;
            }
            CaPO caPO = new CaPO();
            BeanUtils.copyProperties(ca, caPO);
            return caPO;
        }
        if (object instanceof CaSnapshotAgent.ClusterConfigCacheKey) {
            ClusterConfigCacheKey key = (ClusterConfigCacheKey)object;
            ClusterConfig clusterConfig = clusterConfigRepository.getClusterConfig(key.clusterName);
            if (null == clusterConfig) {
                return null;
            }
            ClusterConfigPO clusterConfigPO = new ClusterConfigPO();
            BeanUtils.copyProperties(clusterConfig, clusterConfigPO);
            return clusterConfigPO;
        }
        if (object instanceof CaSnapshotAgent.ClusterNodeCacheKey) {
            ClusterNodeCacheKey key = (ClusterNodeCacheKey)object;
            ClusterNode clusterNode = clusterNodeRepository.getClusterNode(key.nodeName);
            if (null == clusterNode) {
                return null;
            }
            ClusterNodePO clusterNodePO = new ClusterNodePO();
            BeanUtils.copyProperties(clusterNode, clusterNodePO);
            return clusterNodePO;
        }
        log.error("not found load function for cache key:{}", object);
        return null;
    }

    /**
     * the method to batchInsert data into db
     *
     * @param insertList
     * @return
     */
    @Override public boolean batchInsert(List<Pair<Object, Object>> insertList) {
        if (CollectionUtils.isEmpty(insertList)) {
            log.info("[batchInsert]insertMap is empty");
            return true;
        }
        List<CaPO> caPOS = new LinkedList<>();
        List<ClusterConfigPO> clusterConfigPOS = new LinkedList<>();
        List<ClusterNodePO> clusterNodePOS = new LinkedList<>();
        for (Pair<Object, Object> pair : insertList) {
            if (pair.getLeft() instanceof CaSnapshotAgent.CaCacheKey) {
                caPOS.add((CaPO)pair.getRight());
            }
            if (pair.getLeft() instanceof CaSnapshotAgent.ClusterConfigCacheKey) {
                clusterConfigPOS.add((ClusterConfigPO)pair.getRight());
            }
            if (pair.getLeft() instanceof CaSnapshotAgent.ClusterNodeCacheKey) {
                clusterNodePOS.add((ClusterNodePO)pair.getRight());
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
     * @param updateList
     * @return
     */
    @Override public boolean batchUpdate(List<Pair<Object, Object>> updateList) {
        if (CollectionUtils.isEmpty(updateList)) {
            log.info("[updateMap]updateMap is empty");
            return true;
        }
        List<CaPO> caPOS = new LinkedList<>();
        List<ClusterConfigPO> clusterConfigPOS = new LinkedList<>();
        List<ClusterNodePO> clusterNodePOS = new LinkedList<>();
        for (Pair<Object, Object> pair : updateList) {
            if (pair.getLeft() instanceof CaSnapshotAgent.CaCacheKey) {
                caPOS.add((CaPO)pair.getRight());
            }
            if (pair.getLeft() instanceof CaSnapshotAgent.ClusterConfigCacheKey) {
                clusterConfigPOS.add((ClusterConfigPO)pair.getRight());
            }
            if (pair.getLeft() instanceof CaSnapshotAgent.ClusterNodeCacheKey) {
                clusterNodePOS.add((ClusterNodePO)pair.getRight());
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
        private String usage;
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
