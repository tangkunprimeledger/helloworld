package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ContractApiService;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXODBHandler;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXOHandler;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXOSnapshotHandler;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j @Service public class UTXOContextService extends ContractApiService {

    @Autowired
    private UTXOSnapshotHandler utxoSnapshotHandler;
    @Autowired
    private UTXODBHandler utxoDBHandler;


    public UTXOAction getAction() {
        return getContextData(UTXOExecuteContextData.class).getAction();
    }

    /**
     * get utxo action type
     * @param name
     * @return
     */
    public UTXOActionTypeEnum getUTXOActionType(String name) {
        return UTXOActionTypeEnum.getUTXOActionTypeEnumByName(name);
    }

    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
   public List<TxOutPO> queryTxOutList(List<TxIn> inputList){
        log.info("When process UTXO contract  querying queryTxOutList by inputList:{}", inputList);
        UTXOHandler utxoHandler;
        if (getContext().isValidateStage()) {
            // form memory
            utxoHandler = utxoSnapshotHandler;
        } else {
            // form db
            utxoHandler = utxoDBHandler;
        }
        return utxoHandler.queryTxOutList(inputList);
    }
}
