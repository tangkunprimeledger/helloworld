package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.contract.AccountContractBindingRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    public List<AccountContractBinding> get(String accountNo) {
        return (List<AccountContractBinding>) snapshot.get(SnapshotBizKeyEnum.CONTRACT, new AccountContractBindingCacheKey(accountNo));
    }

    public void put(String accountNo, List<AccountContractBinding> bindings) {
        snapshot.put(SnapshotBizKeyEnum.CONTRACT, new AccountContractBindingCacheKey(accountNo), bindings);
    }

    public void put(String accountNo, AccountContractBinding binding) {
        List<AccountContractBinding> list = this.get(accountNo);
        if (null == list) {
            list = new ArrayList<>();
        }
        list.add(binding);
        this.put(accountNo, list);
    }

    @Override public Object query(Object object) {
        List<AccountContractBinding> bindings = repository.queryListByAccountNo(object.toString());
        return bindings;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AccountContractBindingCacheKey extends BaseBO {
        private String accountNo;
    }
}
