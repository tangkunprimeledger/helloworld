package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.ca.Ca;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author WangQuanzhou
 * @desc ca snapshot agent
 * @date 2018/6/6 11:29
 */
@Slf4j @Component public class CaSnapshotAgent implements CacheLoader {

    @Autowired SnapshotService snapshot;
    @Autowired CaRepository caRepository;

    private <T> T get(Object key) {
        return (T)snapshot.get(SnapshotBizKeyEnum.CA, key);
    }

    private void put(Object key, Object object) {
        snapshot.put(SnapshotBizKeyEnum.CA, key, object);
    }

    /**
     * query CA
     *
     * @param user
     * @return
     */
    public Ca getCa(String user) {
        return get(new CaCachKey(user));
    }

    /**
     * save CA
     *
     * @param ca
     */
    public void saveCa(Ca ca) {
        put(new CaSnapshotAgent.CaCachKey(ca.getUser()), ca);
    }

    /**
     * when cache is not exists,load from db
     */
    @Override public Object query(Object object) {
        if (object instanceof CaSnapshotAgent.CaCachKey) {
            CaCachKey key = (CaCachKey)object;
            return caRepository.getCa(key.getUser());
        }
        log.error("not found load function for cache key:{}", object);
        return null;
    }

    /**
     * the cache key of data identity
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor public static class CaCachKey extends BaseBO {
        private String user;
    }
}
