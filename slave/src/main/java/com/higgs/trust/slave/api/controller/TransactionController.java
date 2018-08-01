package com.higgs.trust.slave.api.controller;

import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * transaction tx
 *
 * @author lingchao
 * @create 2018年04月18日14:29
 */
@RequestMapping(value = "/transaction")
@RestController
@Slf4j
public class TransactionController{
    @Autowired
    private BlockChainService blockChainService;

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
        return blockChainService.submitTransaction(transaction);
    }

    @RequestMapping(value = "/master/submit", method = RequestMethod.POST)
    @ResponseBody
    public RespData masterReceive(@RequestBody List<SignedTransaction> transactions) {
        if (log.isDebugEnabled()) {
            log.debug("master receive transactions, parameter :{}", transactions);
        }
        return blockChainService.submitToMaster(transactions);
    }
}
