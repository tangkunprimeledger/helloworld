package com.higgs.trust.rs.core.contract;

import com.higgs.trust.contract.ContractApiService;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.core.service.contract.UTXOExecuteContextData;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/6/15
 */
@Slf4j
@Service
public class RsUTXOContextService extends ContractApiService {

    @Autowired
    private RsBlockChainService rsBlockChainService;

    public UTXOAction getAction() {
        return getContextData(UTXOExecuteContextData.class).getAction();
    }

    /**
     * get utxo action type
     *
     * @param name
     * @return
     */
    public UTXOActionTypeEnum getUTXOActionType(String name) {
        return rsBlockChainService.getUTXOActionType(name);
    }

    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    public List<UTXO> queryUTXOList(List<TxIn> inputList) {
        return rsBlockChainService.queryUTXOList(inputList);
    }
}