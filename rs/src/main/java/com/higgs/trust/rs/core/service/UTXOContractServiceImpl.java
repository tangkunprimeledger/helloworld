package com.higgs.trust.rs.core.service;

import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.rs.core.api.UTXOContractService;
import com.higgs.trust.rs.core.contract.RsUTXOSmartContract;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.contract.UTXOExecuteContextData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * @param coreTransaction
     * @return
     */
    @Override
    public boolean process(CoreTransaction coreTransaction) {
        //check arguments
        if (null == coreTransaction) {
            log.error("process for contract arguments error, coreTransaction is null");
            throw new IllegalArgumentException("process for contract arguments error, coreTransaction is null");
        }
        return processContract(coreTransaction.getActionList());

    }

    /**
     * process contract
     * @param actionList
     * @return
     */
    private boolean processContract(List<Action> actionList) {
        if (CollectionUtils.isEmpty(actionList)) {
            return true;
        }

        //when the action is UTXO we execute contract, otherwise not .
        //if there is no UTXO action return true.
        for (Action action : actionList) {
            if (action.getType() != ActionTypeEnum.UTXO) {
                continue;
            }
            //execute contract
            UTXOAction utxoAction = (UTXOAction) action;
            ExecuteContextData data = new UTXOExecuteContextData().setAction(utxoAction);
            if (!rsUTXOSmartContract.execute(utxoAction.getContractAddress(), data)) {
                log.info("UTXO contract process result is not pass");
                return false;
            }
        }
        return true;
    }
}
