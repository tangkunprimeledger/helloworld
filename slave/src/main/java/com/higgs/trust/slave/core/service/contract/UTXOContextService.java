package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ContractApiService;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXOSnapshotHandler;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import com.higgs.trust.zkproof.EncryptAmount;

@Slf4j
@Service
public class UTXOContextService extends ContractApiService {

    @Autowired
    private UTXOSnapshotHandler utxoSnapshotHandler;


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
        return UTXOActionTypeEnum.getUTXOActionTypeEnumByName(name);
    }

    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    public List<UTXO> queryUTXOList(List<TxIn> inputList) {
        log.info("When process UTXO contract  querying queryTxOutList by inputList:{}", inputList);
        return utxoSnapshotHandler.queryUTXOList(inputList);
    }

    /**
     * add two big decimal data
     * usually used by add  amount
     * return  augend + addend
     *
     * @param addend
     * @param augend
     * @return
     */
    public static BigDecimal add (String augend, String addend) {
        BigDecimal a = null;
        BigDecimal b = null;
        try {
            a = new BigDecimal(augend);
            b = new BigDecimal(addend);
        }catch (Throwable e){
            log.error("UTXO context NumberFormatException in for method add, when execute contract");
            throw new SlaveException(SlaveErrorEnum.SLAVE_UTXO_CONTRACT_PROCESS_FAIL_ERROR);
        }
        return a.add(b);
    }

    public static String cipherAdd (String em1, String em2){
        return EncryptAmount.cipherAdd(em1, em2);
    }

    /**
     * subtract two data
     * usually used by subtract  amount
     * return  minuend -  reduction
     *
     * @param minuend
     * @param reduction
     * @return
     */
    public BigDecimal subtract(String minuend, String reduction) {
        BigDecimal a = null;
        BigDecimal b = null;
        try {
            a = new BigDecimal(minuend);
            b = new BigDecimal(reduction);
        }catch (Throwable e){
            log.error("UTXO context NumberFormatException in for method subtract, when execute contract");
            throw new SlaveException(SlaveErrorEnum.SLAVE_UTXO_CONTRACT_PROCESS_FAIL_ERROR);
        }
        return a.subtract(b);
    }

    /**
     * compare two big decimal
     * if a > b  then return 1
     * if a == b then return 0
     * if a < b then return -1
     *
     * @param a
     * @param b
     * @return
     */
    public int compare(String a, String b) {
        BigDecimal a0 = null;
        BigDecimal b0 = null;
        try {
            a0 = new BigDecimal(a);
            b0 = new BigDecimal(b);
        }catch (Throwable e){
            log.error("UTXO context NumberFormatException in for method compare, when execute contract");
            throw new SlaveException(SlaveErrorEnum.SLAVE_UTXO_CONTRACT_PROCESS_FAIL_ERROR);
        }
        return a0.compareTo(b0);
    }
}
