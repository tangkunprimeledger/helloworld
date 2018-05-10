package com.higgs.trust.slave._interface.transaction;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.core.service.pending.PendingState;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@Slf4j public class TransactionSubmitTest extends InterfaceCommonTest {
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/transaction/submit/";

    @Autowired private BlockChainService blockChainService;

    @Override protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void paramValidate(Map<?, ?> param) {
        log.info("[paramValidate]param:{}", param);
        try {
            RespData respData = blockChainService.submitTransactions(getTxList(param));
            assertEquals(getErrMsg(respData), getAssertData(param));
        } catch (Exception e){
            log.info("has error:{}", e.getMessage());
            if (e instanceof  NullPointerException) {
                assertEquals(e.getMessage(), param.get("assert"));
            } else {
                assertEquals(e.getMessage(), getAssertData(param));
            }
        }
    }

    @Test(dataProvider = "defaultProvider", priority = 2) public void testRegular(Map<?, ?> param) {
        log.info("[testRegular]param:{}", param);
        RespData respData = blockChainService.submitTransactions(getTxList(param));
        assertEquals(getErrMsg(respData), getAssertData(param));
        executeAfterSql(param);
    }

    @Test(dataProvider = "defaultProvider", priority = 3) public void testException(Map<?, ?> param) {
        log.info("[testException]param:{}", param);
        executeBeforeSql(param);
        RespData respData = blockChainService.submitTransactions(getTxList(param));
        assertEquals(getErrMsg(respData), getAssertData(param));
        executeAfterSql(param);
    }

    private List<SignedTransaction> getTxList(Map<?, ?> param) {
        String body = String.valueOf(param.get("body"));
        if (StringUtils.isEmpty(body) || "null".equals(body)) {
            return null;
        }
        body = body.replaceAll("\"@type\":\"com.alibaba.fastjson.JSONObject\",", "");
        return JSON.parseArray(body, SignedTransaction.class);
    }

    private String getErrMsg(RespData respData) {

        if (null == respData.getData()) {
            return "null";
        }

        List<TransactionVO> voList = (List<TransactionVO>)respData.getData();
        return voList.get(0).getErrMsg();
    }
}
