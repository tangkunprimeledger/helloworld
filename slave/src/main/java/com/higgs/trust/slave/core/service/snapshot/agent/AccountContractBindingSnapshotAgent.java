package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.contract.AccountContractBindingRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * the snapshot agent of AccountContractBinding
 * @author duhongming
 * @date 2018-04-19
 */
@Slf4j @Component public class AccountContractBindingSnapshotAgent implements CacheLoader {

    @Autowired SnapshotService snapshot;
    @Autowired AccountContractBindingRepository repository;

    public List<AccountContractBinding> get(String key) {
        return (List<AccountContractBinding>) snapshot.get(SnapshotBizKeyEnum.CONTRACT, key);
    }

    public void put(String key, List<AccountContractBinding> bindings) {
        snapshot.put(SnapshotBizKeyEnum.CONTRACT, key, bindings);
    }

    public void put(String key, AccountContractBinding binding) {
        List<AccountContractBinding> list = get(key);
        if (null == list) {
            list = new ArrayList<>();
        }
        list.add(binding);
        snapshot.put(SnapshotBizKeyEnum.CONTRACT, key, list);
    }

    @Override public Object query(Object object) {
        List<AccountContractBinding> bindings = repository.queryListByAccountNo(object.toString());
        return bindings;
    }
}
