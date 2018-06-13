package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.dao.po.contract.ContractPO;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.contract.Contract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author duhongming
 * @date 2018-04-12
 */
@Slf4j
@Component
public class ContractSnapshotAgent implements CacheLoader {

    @Autowired
    SnapshotService snapshot;
    @Autowired
    private ContractRepository contractRepository;

    public Contract get(String key) {
        return (Contract) snapshot.get(SnapshotBizKeyEnum.CONTRACT, new ContractCacheKey(key));
    }

    public void insert(String key, Contract contract) {
        snapshot.insert(SnapshotBizKeyEnum.CONTRACT, new ContractCacheKey(key), contract);
    }

    @Override
    public Object query(Object object) {
        ContractCacheKey key = (ContractCacheKey) object;
        return contractRepository.queryByAddress(key.getAddress());
    }

    /**
     * the method to batchInsert data into db
     *
     * @param insertMap
     * @return
     */
    @Override
    public boolean batchInsert(Map<Object, Object> insertMap) {
        List<ContractPO> list = new ArrayList<>(insertMap.size());
        insertMap.forEach((key, value) -> {
            Contract contract = (Contract)value;
            list.add(BeanConvertor.convertBean(contract, ContractPO.class));
        });
        return contractRepository.batchInsert(list);
    }

    /**
     * the method to batchUpdate data into db
     *
     * @param updateMap
     * @return
     */
    @Override
    public boolean batchUpdate(Map<Object, Object> updateMap) {
        throw new NotImplementedException();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractCacheKey extends BaseBO {
        private String address;
    }
}
