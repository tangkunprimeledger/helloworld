package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.repository.RsPubKeyRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author tangfashuang
 * @description an agent for policy and rs snapshot
 * @date 2018-04-12 17:28
 */
@Service
@Slf4j
public class ManageSnapshotAgent implements CacheLoader {

    @Autowired
    private SnapshotService snapshot;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private RsPubKeyRepository rsPubKeyRepository;

    private <T> T get(Object key) {
        return (T) snapshot.get(SnapshotBizKeyEnum.MANAGE, key);
    }

    //TODO You  should provide insert and update method for yourself to use by using snapshot insert or uodate method .
    private void put(Object key, Object object) {

        //snapshot.put(SnapshotBizKeyEnum.MANAGE,key,object);
    }

    @Override
    public Object query(Object obj) {
        if (obj instanceof PolicyCacheKey) {
            PolicyCacheKey policyCacheKey = (PolicyCacheKey) obj;
            return policyRepository.getPolicyById(policyCacheKey.getPolicyId());
        } else if (obj instanceof RsPubKeyCacheKey) {
            RsPubKeyCacheKey rsPubKeyCacheKey = (RsPubKeyCacheKey) obj;
            return rsPubKeyRepository.queryByRsId(rsPubKeyCacheKey.getRsId());
        }

        return null;
    }

    /**
     * the method to bachInsert data into db
     *
     * @param insertMap
     * @return
     */
    //TODO to implements your own bachInsert method for db
    @Override
    public boolean bachInsert(Map<Object, Object> insertMap) {
        return false;
    }

    /**
     * the method to bachUpdate data into db
     *
     * @param updateMap
     * @return
     */
    //TODO to implements your own bachUpdate method for db
    @Override
    public boolean bachUpdate(Map<Object, Object> updateMap) {
        return false;
    }

    public Policy getPolicy(String policyId) {
        return get(new PolicyCacheKey(policyId));
    }

    public RsPubKey getRsPubKey(String rsId) {
        return get(new PolicyCacheKey(rsId));
    }

    public Policy registerPolicy(RegisterPolicy registerPolicy) {
        Policy policy = policyRepository.convertActionToPolicy(registerPolicy);
        put(new PolicyCacheKey(policy.getPolicyId()), policy);
        return policy;
    }

    public RsPubKey registerRs(RegisterRS registerRS) {
        RsPubKey rsPubKey = rsPubKeyRepository.convertActionToRsPubKey(registerRS);
        put(new RsPubKeyCacheKey(rsPubKey.getRsId()), rsPubKey);
        return rsPubKey;
    }

    /**
     * the cache key of policy
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyCacheKey extends BaseBO {
        private String policyId;
    }

    /**
     * the cache key of rsPubKey
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RsPubKeyCacheKey extends BaseBO {
        private String rsId;
    }
}
