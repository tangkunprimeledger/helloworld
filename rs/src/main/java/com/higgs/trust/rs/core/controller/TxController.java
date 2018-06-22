package com.higgs.trust.rs.core.controller;

import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.vo.RespData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuyu
 * @description
 * @date 2018-06-22
 */
@RestController public class TxController {
    @Autowired private CoreTransactionService coreTransactionService;

    @RequestMapping(value = "/submitTx") RespData submitTx(@RequestBody RsCoreTxVO coreTx) {
        coreTransactionService.submitTx(coreTx);
        return coreTransactionService.syncWait(coreTx.getTxId(), coreTx.isForEnd());
    }
}
