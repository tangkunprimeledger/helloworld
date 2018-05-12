package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.contract.Contract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author duhongming
 * @date 2018-04-12
 */
@Slf4j @Component public class ContractSnapshotAgent implements CacheLoader {

    @Autowired SnapshotService snapshot;
    @Autowired private ContractRepository contractRepository;

    public Contract get(String key) {
        return (Contract) snapshot.get(SnapshotBizKeyEnum.CONTRACT, new ContractCacheKey(key));
    }

    public void put(String key, Contract contract) {
        snapshot.put(SnapshotBizKeyEnum.CONTRACT, new ContractCacheKey(key), contract);
    }

    @Override public Object query(Object object) {
        ContractCacheKey key = (ContractCacheKey) object;
        return contractRepository.queryByAddress(key.getAddress());
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ContractCacheKey extends BaseBO {
        private String address;
    }
}
