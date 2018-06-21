package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.vo.account.CurrencyVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.SnowflakeIdWorker;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liuyu
 * @description
 * @date 2018-06-21
 */
@Service @Slf4j public class AccountService {
    @Autowired
    private CoreTransactionService coreTransactionService;
    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    public RespData issueCurrency(CurrencyVO currencyVO){
        String txId = String.valueOf(snowflakeIdWorker.nextId());

        IssueCurrency issueCurrency = new IssueCurrency();
        issueCurrency.setIndex(1);
        issueCurrency.setType(ActionTypeEnum.ISSUE_CURRENCY);
        issueCurrency.setCurrencyName(currencyVO.getCurrency());
        issueCurrency.setRemark(currencyVO.getRemark());

        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setTxId(txId);
//        coreTransaction.setPolicyId();
        coreTransaction.setBizModel(new JSONObject());
        coreTransaction.setActionList(Lists.newArrayList(issueCurrency));
        coreTransactionService.submitTx(coreTransaction);
        return coreTransactionService.syncWait(txId,false);
    }

}
