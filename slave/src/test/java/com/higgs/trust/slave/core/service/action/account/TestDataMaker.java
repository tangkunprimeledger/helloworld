package com.higgs.trust.slave.core.service.action.account;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.crypto.rsa.Rsa;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.account.*;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.enums.BlockVersionEnum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-17
 */
public class TestDataMaker {

    private static final String priKey1 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIsMM2JkqnQACN+RUqRF4UkW58XnjpjA+RP4mnA4uD9KCjlJeTxvY0yHDqsvIaiXr7Ge0QB1VAKq0xit2BWHfVU/XlO1tqK+ea7YxooeoglvOkddiJiYTZUNjKM5AhttG950PpzrmeUcl9YGEZ/DwKKee+8tqaDWdIEHBnplO6mVAgMBAAECgYBtrWwCmoDRCw30uv5C0VQIgObE9gdGekB9/kRjbHn4ggBae5gDkaDzxjxNztlv0GYnZqxY/jML/46PEuE06jBzGcOlBuobQJJ38pTg0pnNVHbkTckxfUIr1MYUDhtO18tJZUZuMbYMwwgZ9K9E0N8kjKXk+rRx+BDjlbxNPds6KQJBAMLS/HCXjAfJlzSEWqkBavAKoW+bBhZlkTH+DoNk/KidASgdFBqtPUf5w3U+j8dK4nvt8R9X7zGxRAYXpDGHUucCQQC2tZlmL858suIA/+XfQVGoKOEvLlI5tGNDLXlDaKldY8UZGqxcyKaOsqEWMQnJCUy/0zTariN7kNssptYm04wjAkB3qVlt2lcizVn24rhAh+NjzlO7le8WQIn+t7m4UIWzFsQIHFwlynQSSkEYOTXcRY14avwnsT30Opm6WDj8Rs7PAkBytzqFSmbfLIFyFzmBH0Xhyyj3sqG10WixeQ+2HzSXiljqFjE6YFETL1yszkVSkCA8IKQC2Ws13hF+y5GR9yj5AkBAeUJj/a8wpdxJCufDpoaVUsB/XGK9XCqlGZvSy2TrjWLLBjZ3jiyjTlqIssfqI/IiJ4H2peocDaHXjFT0m+Av";
    private static final String priKey2 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJE5lYYWx1AE5E0XaxmQvg2putYOrya+Azd+QCHVA0rE8vUiXAuFia7TgjGQjhFO66SrcOJ3W2xSeHw6F1ApvZ8kfLB3ZsZT1e54QaWKU0ae0fcmk9dgrkpaniLCbd3PP8A+UUjwNeexIDjThYSzzatNGg06qdxgSCMc9bX7jwPlAgMBAAECgYA5Vtcmvk+r1IKfvaNX0MJ5eo5+fgXB8jwq6PpBYW2PU/vptctJ8UvPb0t0bnLpepOnzNkhUacTOezAf988k35+gw9Vrh6rXG4x7cZ65qbQOP+Xh0sx3YElyZKUBJzl5CMjNzT5ANc/QdpCD8LOOiPF4xpcHqKih74NGXc8hQv1AQJBAMNm1i6AC/oJM7XDnXPyswNVyjjIG+wi9xBgMGlt8JLH3c7/HblyAPHS/sFnljroVsHOyvoi45aZlIluhZsh6tUCQQC+QygmlnViImHq+MgL6bWaTLjKyTpH6k7zGQxvOxqJlioeWh73wxHAq/depKi8ElMrkEhMBA05ReCJl4Nb8xzRAkA28/HiS/KSVAot4SCj3iqIEpV3mJd5tm+jNFoJHHke3oS71TWH1M79M2if/cDbOkJD6SNea3d0ACcs6185vLUtAkBtrWLw05z5JB7UB/Oxwli4iO+hnlxlZnF6e38Kg8SpeZHwCz18z8tlCPzBZyQJvnqJS1QR1egVku18A4Zqs/txAkAg5kjdDw0v9QoQMr4oHZSuHxaG9I91SRkCPspN8urIg1Wu7cdTKfPdaAtSoU/xp/qpzfX+CPkhv7DoGlBLeo8e";

    private static final String priKey3 =
        "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANDzTWjIRJ6Y3dKT4Z08/QuUMjj3OFSgt8qD9ZFgT3TXik44olP7O0gVJiL+tBtCuqsW6nU2BWt2S/1/SmGVq1dxco1VSCU/Dk7ReBTMRyZBOxfzdMnaTWMbiO+ETodJl3eQbK1miJyVbg7hLe7s/8xiH7AGsKkppW6GC7Kpb4zJAgMBAAECgYBORbYLuGmsF4uQ5ICxjDUmbz9ZA5MAcKwomsIU0UUyecN/hcuZNhWA7Rs6JLuHMroGeTEe8zuYg9n3fgV5BL4H96z3SBSrY+BsCf1CxYGXEVCHzlt6g8575MqtxIlqPXnpKr9S1663EtsCCJ93t5rZmMA7z8bUbFRTcrUsajYzAQJBAPynP0a6Pk5JlF0TW5vbzusZb3CsEdPTp39NxlHEx9v/2xuREti1CSVMhdm8ZDdC5hDoETZn4DTiBAF0Z5it6pkCQQDTt9uSFv16v+62yJIz0KE9EUZrLua1BlfTIyvgBZQ6Lp5ORS2S9iVzfOS77mufysbfGSpmD6Oc5ElY2coUy8GxAkAlFB5zMM4IC0Bc0IR3QTECy77RGE+deMhyJGXghjKWlNwBFa9gYmEvOiXCqKVEfurovEYaZ/A9kpXn6L9zZsKxAkAuym+IdfRHcKu9Uc6eDPnVmT/K6G6si15Vl2xW8mS0ByGNgtRzqlrUj0GuFx9KDXKuU81/CO3L+tgK/vceaXnBAkEAk+OjzXA0KXZGKm+O8/Vl8yiJQpuvpuO4cxy4E7nEAjevFip88p4tO03DVxjyq2Az7457q/T+C/Ohr1X9uS/v/Q==";

    public static Action createRegisterPolicyAction() {
        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setIndex(0);
        registerPolicy.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy.setPolicyId("test-policy-0002-" + System.currentTimeMillis());
        registerPolicy.setPolicyName("测试policy注册");
        List<String> rsIds = new ArrayList<>();
        rsIds.add("TRUST-NODE97");
        rsIds.add("TRUST-NODE100");
        registerPolicy.setRsIds(rsIds);
        return registerPolicy;
    }

    public static Action makeOpenAccountAction(String accountNo, FundDirectionEnum fundDirectionEnum) {
        OpenAccount action = new OpenAccount();
        action.setType(ActionTypeEnum.OPEN_ACCOUNT);
        action.setIndex(0);
        action.setAccountNo(accountNo);
        action.setChainOwner("TRUST");
        action.setDataOwner("TRUST-NODE97");
        action.setCurrency("CNY");
        action.setFundDirection(fundDirectionEnum);
        return action;
    }

    public static Action makeOpertionAction(String debitAccountNo, String creditAccountNo, BigDecimal happenAmount) {
        AccountOperation action = new AccountOperation();
        action.setType(ActionTypeEnum.ACCOUNTING);
        action.setIndex(0);

        List<AccountTradeInfo> debitTradeInfo = new ArrayList<>();
        debitTradeInfo.add(new AccountTradeInfo(debitAccountNo, happenAmount));
        List<AccountTradeInfo> creditTradeInfo = new ArrayList<>();
        creditTradeInfo.add(new AccountTradeInfo(creditAccountNo, happenAmount));

        action.setBizFlowNo("biz_flow_no_" + System.currentTimeMillis());
        action.setDebitTradeInfo(debitTradeInfo);
        action.setCreditTradeInfo(creditTradeInfo);
        action.setAccountDate(new Date());

        return action;
    }

    public static Action makeFreezeAction(String accountNo, int index) {
        AccountFreeze action = new AccountFreeze();
        action.setType(ActionTypeEnum.FREEZE);
        action.setAccountNo(accountNo);
        action.setAmount(new BigDecimal("0.50"));
        action.setBizFlowNo("freeze_flow_no_1_" + index + "_" + System.currentTimeMillis());
        action.setIndex(0);
        return action;
    }

    public static Action makeUnFreezeAction(String accountNo, String bizFlowNo) {
        AccountUnFreeze action = new AccountUnFreeze();
        action.setType(ActionTypeEnum.UNFREEZE);
        action.setAccountNo(accountNo);
        action.setAmount(new BigDecimal("0.10"));
        action.setBizFlowNo(bizFlowNo);
        action.setIndex(0);
        return action;
    }

    public static Action makeCurrencyAction(String currencyName) {
        IssueCurrency action = new IssueCurrency();
        action.setType(ActionTypeEnum.ISSUE_CURRENCY);
        action.setIndex(0);
        action.setCurrencyName(currencyName);
        action.setRemark("this is test");
        return action;
    }

    public static CoreTransaction makeCoreTx(List<Action> actions, int index, InitPolicyEnum policyEnum) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setPolicyId(policyEnum.getPolicyId());
        coreTx.setTxId("tx_id_" + index + "_" + System.currentTimeMillis());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setActionList(actions == null ? new ArrayList<>() : actions);
        coreTx.setBizModel(new JSONObject());
        coreTx.setSender("rs-test1");
        coreTx.setSendTime(new Date());
        coreTx.setLockTime(new Date());
        return coreTx;
    }

    public static CoreTransaction makeCoreTx(List<Action> actions, int index, String policyId, JSONObject bizModel) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setPolicyId(policyId);
        coreTx.setTxId("tx_id_" + actions.get(0).getType().getCode() + "_" + index + "_" + System.currentTimeMillis());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setActionList(actions == null ? new ArrayList<>() : actions);
        coreTx.setBizModel(bizModel);
        coreTx.setSendTime(new Date());
        coreTx.setSender("TRUST-NODE97");
        coreTx.setLockTime(new Date());
        return coreTx;
    }

    public static SignedTransaction makeSignedTx(CoreTransaction coreTransaction) throws Exception {
        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.setCoreTx(coreTransaction);
        String sign = Rsa.sign(JSON.toJSONString(coreTransaction), priKey1);
        String sign1 = Rsa.sign(JSON.toJSONString(coreTransaction), priKey2);
        List<SignInfo> signedList = new ArrayList<>();
        SignInfo signInfo = new SignInfo();
        signInfo.setOwner("owner1");
        signInfo.setSign(sign);
        signedList.add(signInfo);

        signInfo.setOwner("owner2");
        signInfo.setSign(sign1);
        signedList.add(signInfo);

        signedTransaction.setSignatureList(signedList);
        return signedTransaction;
    }

    public static BlockHeader makeBlockHeader() {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(1L);
        blockHeader.setPreviousHash("xxxx");
        blockHeader.setBlockHash("root-hash");
        blockHeader.setBlockTime(System.currentTimeMillis());
        blockHeader.setVersion(BlockVersionEnum.V1.getCode());
        StateRootHash rootHash = new StateRootHash();
        rootHash.setAccountRootHash("account-hash");
        rootHash.setTxRootHash("tx-hash");
        rootHash.setTxReceiptRootHash("tx-receipt-hash");
        rootHash.setPolicyRootHash("policy-hash");
        rootHash.setRsRootHash("rs-root-hash");
        rootHash.setContractRootHash("contract-hash");
        rootHash.setCaRootHash("ca-hash");
        blockHeader.setStateRootHash(rootHash);
        return blockHeader;
    }
}
