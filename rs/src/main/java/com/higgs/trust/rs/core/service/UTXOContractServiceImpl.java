package com.higgs.trust.rs.core.service;

import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.UTXOContractService;
import com.higgs.trust.rs.core.contract.RsUTXOSmartContract;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.contract.UTXOExecuteContextData;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * UTXOContractService impl
 *
 * @author lingchao
 * @create 2018年06月29日1:05
 */
@Slf4j
@Service
public class UTXOContractServiceImpl implements UTXOContractService {
    @Autowired
    private RsUTXOSmartContract rsUTXOSmartContract;

    /**
     * process UTXO contract
     *
     * @param utxoAction
     * @param contractAddress
     * @return
     */
    @Override
    public boolean process(UTXOAction utxoAction, String contractAddress) {
        //check arguments
        if (null == utxoAction || StringUtils.isBlank(contractAddress)) {
            log.error("process for contract arguments error for action={}, contractAddress={}", utxoAction, contractAddress);
            throw new IllegalArgumentException("process for contract arguments error");
        }
        //TODO lingchao 检查是否需要检查类型
        if (utxoAction.getType() != ActionTypeEnum.UTXO) {
            log.error("The process  action for contract is not UTXO action ,in fact it is {}", utxoAction.getType());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CONTRACT_ACTION_TYPE_ILLEGAL_ERROR);
        }
        //execute contract
        ExecuteContextData data = new UTXOExecuteContextData().setAction(utxoAction);
        return rsUTXOSmartContract.execute(utxoAction.getContractAddress(), data);
    }
}
