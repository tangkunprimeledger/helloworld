package com.higgs.trust.slave.api.controller;

import com.higgs.trust.evmcontract.core.TransactionResultInfo;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.core.Blockchain;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * transaction tx
 *
 * @author lingchao
 * @create 2018年04月18日14:29
 */
@RequestMapping(value = "/transaction")
@RestController
@Slf4j
public class TransactionController {
    @Autowired
    private BlockChainService blockChainService;
    @Autowired
    private Blockchain blockchain;

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    @ResponseBody
    public RespData submitTransactions(@RequestBody List<SignedTransaction> transactions) {
        if (log.isDebugEnabled()) {
            log.debug("submit transactions receive parameter :{}", transactions);
        }
        return blockChainService.submitTransactions(transactions);
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST)
    @ResponseBody
    public RespData submitTransaction(@RequestBody SignedTransaction transaction) {

        if (log.isDebugEnabled()) {
            log.debug("submit transaction receive parameter :{}", transaction);
        }

        List<SignedTransaction> signedTransactionList = new ArrayList<>();
        signedTransactionList.add(transaction);
        return blockChainService.submitTransactions(signedTransactionList);
    }

    @RequestMapping(value = "/master/submit", method = RequestMethod.POST)
    @ResponseBody
    public RespData<List<TransactionVO>> masterReceive(@RequestBody List<SignedTransaction> transactions) {
        if (log.isDebugEnabled()) {
            log.debug("master receive transactions, parameter :{}", transactions);
        }
        return blockChainService.submitToMaster(transactions);
    }


    @GetMapping("result/{txId}")
    public Map<String, Object> queryResult(@PathVariable("txId") String txId) {
        TransactionResultInfo resultInfo =  blockchain.getTransactionResultInfo(txId);
        return resultInfo.toMap();
    }
}
