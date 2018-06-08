package com.higgs.trust.slave._interface.transaction;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.transaction.TransactionExecutor;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.context.PackContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@Slf4j public class TransactionExecuteTest extends InterfaceCommonTest {
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/transaction/execute/";

    @Autowired private TransactionExecutor transactionExecutor;

    @Autowired private SnapshotService snapshotService;

    @Override protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }

    /**
     * InterfaceCommonTest has already snapshotService.startTransaction
     */
    @BeforeMethod public void before() {
        snapshotService.destroy();
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void paramValidate(Map<?, ?> param) {
        log.info("[paramValidate]param:{}", param);
        executeValidate(param);
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void testRegular(Map<?, ?> param) {
        log.info("[testRegular]param:{}", param);
        executeValidate(param);
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void testRegularPersist(Map<?, ?> param) {
        log.info("[testRegularPersist]param:{}", param);
        executeBeforeSql(param);
        SignedTransaction signedTx = getBodyData(param, SignedTransaction.class);
        PackContext packContext = new PackContext(null, null);
        packContext.setCurrentTransaction(signedTx);

        try {

            transactionExecutor.process(packContext, packContext.getRsPubKeyMap());
        } catch (Exception e) {
            assertEquals(e.getMessage(), param.get("assert"));
        }

        List<JSONArray> list = executeQuerySql(param);

        assertEquals(param.get("assert"), list.size());

        executeAfterSql(param);
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void generateSign(Map<?, ?> param) {
        log.info("[generateSign]param:{}", param);

        CoreTransaction coreTx = getBodyData(param, CoreTransaction.class);
        String signature = SignUtils.sign(JSON.toJSONString(coreTx),
            "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIsMM2JkqnQACN+RUqRF4UkW58XnjpjA+RP4mnA4uD9KCjlJeTxvY0yHDqsvIaiXr7Ge0QB1VAKq0xit2BWHfVU/XlO1tqK+ea7YxooeoglvOkddiJiYTZUNjKM5AhttG950PpzrmeUcl9YGEZ/DwKKee+8tqaDWdIEHBnplO6mVAgMBAAECgYBtrWwCmoDRCw30uv5C0VQIgObE9gdGekB9/kRjbHn4ggBae5gDkaDzxjxNztlv0GYnZqxY/jML/46PEuE06jBzGcOlBuobQJJ38pTg0pnNVHbkTckxfUIr1MYUDhtO18tJZUZuMbYMwwgZ9K9E0N8kjKXk+rRx+BDjlbxNPds6KQJBAMLS/HCXjAfJlzSEWqkBavAKoW+bBhZlkTH+DoNk/KidASgdFBqtPUf5w3U+j8dK4nvt8R9X7zGxRAYXpDGHUucCQQC2tZlmL858suIA/+XfQVGoKOEvLlI5tGNDLXlDaKldY8UZGqxcyKaOsqEWMQnJCUy/0zTariN7kNssptYm04wjAkB3qVlt2lcizVn24rhAh+NjzlO7le8WQIn+t7m4UIWzFsQIHFwlynQSSkEYOTXcRY14avwnsT30Opm6WDj8Rs7PAkBytzqFSmbfLIFyFzmBH0Xhyyj3sqG10WixeQ+2HzSXiljqFjE6YFETL1yszkVSkCA8IKQC2Ws13hF+y5GR9yj5AkBAeUJj/a8wpdxJCufDpoaVUsB/XGK9XCqlGZvSy2TrjWLLBjZ3jiyjTlqIssfqI/IiJ4H2peocDaHXjFT0m+Av");
        System.out.println(signature);
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void testException(Map<?, ?> param) {
        log.info("[testException]param:{}", param);
        executeValidate(param);
    }

    private void executeValidate(Map<?, ?> param) {

        executeBeforeSql(param);
        SignedTransaction signedTx = getBodyData(param, SignedTransaction.class);
        PackContext packContext = new PackContext(null, null);
        packContext.setCurrentTransaction(signedTx);

        try {

//            transactionExecutor.validate(packContext);
        } catch (Exception e) {
            assertEquals(e.getMessage(), param.get("assert"));
        }

        executeAfterSql(param);
    }

}
