package com.higgs.trust.slave.core.repository.contract;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.contract.ContractDao;
import com.higgs.trust.slave.dao.po.contract.ContractPO;
import com.higgs.trust.slave.model.bo.contract.Contract;
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
        ContractPO contractPO = BeanConvertor.convertBean(contract, ContractPO.class);

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
        Contract contract = BeanConvertor.convertBean(po, Contract.class);
        return contract;
    }
}