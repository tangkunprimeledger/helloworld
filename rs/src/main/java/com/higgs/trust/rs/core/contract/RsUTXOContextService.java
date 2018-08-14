package com.higgs.trust.rs.core.contract;

import com.higgs.trust.contract.ContractApiService;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.core.service.contract.UTXOExecuteContextData;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
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
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CONTRACT_EXECUTE_ERROR);
        }
        return a.add(b);
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
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CONTRACT_EXECUTE_ERROR);
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
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CONTRACT_EXECUTE_ERROR);
        }
        return a0.compareTo(b0);
    }

}