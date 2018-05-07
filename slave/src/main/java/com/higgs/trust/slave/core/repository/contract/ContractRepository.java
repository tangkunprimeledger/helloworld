package com.higgs.trust.slave.core.repository.contract;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.contract.ContractDao;
import com.higgs.trust.slave.dao.po.contract.ContractPO;
import com.higgs.trust.slave.model.bo.Contract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/**
 * Contract Repository
 * @author duhongming
 * @date 2018-04-12
 */
@Repository
@Slf4j
public class ContractRepository {

    @Autowired private ContractDao contractDao;

    /**
     * deploy contract
     * @param contract
     */
    public void deploy(Contract contract) {
        ContractPO contractPO = new ContractPO();
        contractPO.setAddress(contract.getAddress());
        contractPO.setLanguage(contract.getLanguage());
        contractPO.setCode(contract.getCode());
        contractPO.setCreateTime(contract.getCreateTime());

        try {
            contractDao.add(contractPO);
        } catch (DuplicateKeyException e) {
            log.error("Insert contract fail, because there is DuplicateKeyException for dataidentity:",
                    contract);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT, e);
        }
    }

    public Contract queryByAddress(String address) {
        ContractPO po = contractDao.queryByAddress(address);
        if (null == po) {
            return null;
        }
        Contract contract = new Contract();
        contract.setAddress(po.getAddress());
        contract.setCode(po.getCode());
        contract.setLanguage(po.getLanguage());
        contract.setCreateTime(po.getCreateTime());
        return contract;
    }
}