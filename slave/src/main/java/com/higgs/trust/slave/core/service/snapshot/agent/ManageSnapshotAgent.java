package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.exception.SnapshotException;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.repository.RsNodeRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.dao.po.manage.PolicyPO;
import com.higgs.trust.slave.dao.po.manage.RsNodePO;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tangfashuang
 * @description an agent for policy and rs snapshot
 * @date 2018-04-12 17:28
 */
@Service
@Slf4j
public class ManageSnapshotAgent implements CacheLoader {

    @Autowired private SnapshotService snapshot;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private RsNodeRepository rsNodeRepository;

    private <T> T get(Object key){
        return (T)snapshot.get(SnapshotBizKeyEnum.MANAGE,key);
    }

    private void insert(Object key, Object object) {
        snapshot.insert(SnapshotBizKeyEnum.MANAGE,key,object);
    }

    private void update(Object key, Object object) {
        snapshot.update(SnapshotBizKeyEnum.MANAGE,key,object);
    }

    @Override public Object query(Object obj) {
        if (obj instanceof PolicyCacheKey) {
            PolicyCacheKey policyCacheKey = (PolicyCacheKey)obj;
            return policyRepository.convertPolicyToPolicyPO(
                policyRepository.getPolicyById(policyCacheKey.getPolicyId()));
        } else if (obj instanceof RsNodeCacheKey) {
            RsNodeCacheKey rsNodeCacheKey = (RsNodeCacheKey)obj;
            return rsNodeRepository.convertRsNodeToRsNodePO(
                rsNodeRepository.queryByRsId(rsNodeCacheKey.getRsId()));
        }

        return null;
    }

    /**
     * the method to bachInsert data into db
     *
     * @param insertList
     * @return
     */
    @Override
    public boolean batchInsert(List<Pair<Object, Object>> insertList) {
        if (CollectionUtils.isEmpty(insertList)){
            return true;
        }

        List<RsNodePO> rsNodePOList = new ArrayList<>();
        List<PolicyPO> policyPOList = new ArrayList<>();
        for (Pair<Object, Object> pair : insertList) {
            if ((pair.getLeft() instanceof  RsNodeCacheKey)) {
                rsNodePOList.add((RsNodePO)pair.getRight());
            } else if ((pair.getLeft() instanceof  PolicyCacheKey)) {
                policyPOList.add((PolicyPO)pair.getRight());
            } else {
                log.error("insert key is not type of RsNodeCacheKey or PolicyCacheKey");
                throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_DATA_TYPE_ERROR_EXCEPTION);
            }
        }
        if (!CollectionUtils.isEmpty(rsNodePOList)) {
            return rsNodeRepository.batchInsert(rsNodePOList) == rsNodePOList.size();
        }

        if (!CollectionUtils.isEmpty(policyPOList)) {
            return policyRepository.batchInsert(policyPOList) == policyPOList.size();
        }
        return true;
    }

    /**
     * the method to bachUpdate data into db
     *
     * @param updateList
     * @return
     */
    @Override
    public boolean batchUpdate(List<Pair<Object, Object>> updateList) {
        if (CollectionUtils.isEmpty(updateList)){
            return true;
        }

        List<RsNodePO> rsNodePOList = new ArrayList<>();
        for (Pair<Object, Object> pair : updateList) {
            if ((pair.getLeft() instanceof  RsNodeCacheKey)) {
                rsNodePOList.add((RsNodePO)pair.getRight());
            } else {
                log.error("insert key is not type of RsNodeCacheKey");
                throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_DATA_TYPE_ERROR_EXCEPTION);
            }
        }
        if (!CollectionUtils.isEmpty(rsNodePOList)) {
            rsNodeRepository.batchUpdate(rsNodePOList);
        }

        return true;
    }

    public Policy getPolicy(String policyId) {
        PolicyPO po = get(new PolicyCacheKey(policyId));
        if (null == po) {
            return null;
        }
        return policyRepository.convertPolicyPOToPolicy(po);
    }

    public RsNode getRsNode(String rsId) {
        RsNodePO po = get(new RsNodeCacheKey(rsId));
        if (null == po) {
            return null;
        }
        return rsNodeRepository.convertRsNodePOtoRsNode(po);
    }

    public Policy registerPolicy(RegisterPolicy registerPolicy) {
        Policy policy = policyRepository.convertActionToPolicy(registerPolicy);

        insert(new PolicyCacheKey(policy.getPolicyId()),
            policyRepository.convertPolicyToPolicyPO(policy));
        return policy;
    }

    public RsNode registerRs(RegisterRS registerRS) {
        RsNode rsNode = rsNodeRepository.convertActionToRsNode(registerRS);
        insert(new RsNodeCacheKey(rsNode.getRsId()),
            rsNodeRepository.convertRsNodeToRsNodePO(rsNode));
        return rsNode;
    }

    public void updateRs(RsNode rsNode) {
        update(new RsNodeCacheKey(rsNode.getRsId()),
            rsNodeRepository.convertRsNodeToRsNodePO(rsNode));
    }

    /**
     * the cache key of policy
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor public static class PolicyCacheKey extends BaseBO {
        private String policyId;
    }

    /**
     * the cache key of rs node
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor public static class RsNodeCacheKey extends BaseBO {
        private String rsId;
    }
}
