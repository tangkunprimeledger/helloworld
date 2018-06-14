package com.higgs.trust.slave.core.service.pack;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/14
 *
 */
public class PackageServiceImplTest extends BaseTest{

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageRepository packageRepository;

    private Package packageToRecieve;

    private static final List<SignedTransaction> signedTxList = new ArrayList<>();
    private static final String priKey1 ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIsMM2JkqnQACN+RUqRF4UkW58XnjpjA+RP4mnA4uD9KCjlJeTxvY0yHDqsvIaiXr7Ge0QB1VAKq0xit2BWHfVU/XlO1tqK+ea7YxooeoglvOkddiJiYTZUNjKM5AhttG950PpzrmeUcl9YGEZ/DwKKee+8tqaDWdIEHBnplO6mVAgMBAAECgYBtrWwCmoDRCw30uv5C0VQIgObE9gdGekB9/kRjbHn4ggBae5gDkaDzxjxNztlv0GYnZqxY/jML/46PEuE06jBzGcOlBuobQJJ38pTg0pnNVHbkTckxfUIr1MYUDhtO18tJZUZuMbYMwwgZ9K9E0N8kjKXk+rRx+BDjlbxNPds6KQJBAMLS/HCXjAfJlzSEWqkBavAKoW+bBhZlkTH+DoNk/KidASgdFBqtPUf5w3U+j8dK4nvt8R9X7zGxRAYXpDGHUucCQQC2tZlmL858suIA/+XfQVGoKOEvLlI5tGNDLXlDaKldY8UZGqxcyKaOsqEWMQnJCUy/0zTariN7kNssptYm04wjAkB3qVlt2lcizVn24rhAh+NjzlO7le8WQIn+t7m4UIWzFsQIHFwlynQSSkEYOTXcRY14avwnsT30Opm6WDj8Rs7PAkBytzqFSmbfLIFyFzmBH0Xhyyj3sqG10WixeQ+2HzSXiljqFjE6YFETL1yszkVSkCA8IKQC2Ws13hF+y5GR9yj5AkBAeUJj/a8wpdxJCufDpoaVUsB/XGK9XCqlGZvSy2TrjWLLBjZ3jiyjTlqIssfqI/IiJ4H2peocDaHXjFT0m+Av";
    private static final String priKey2 ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJE5lYYWx1AE5E0XaxmQvg2putYOrya+Azd+QCHVA0rE8vUiXAuFia7TgjGQjhFO66SrcOJ3W2xSeHw6F1ApvZ8kfLB3ZsZT1e54QaWKU0ae0fcmk9dgrkpaniLCbd3PP8A+UUjwNeexIDjThYSzzatNGg06qdxgSCMc9bX7jwPlAgMBAAECgYA5Vtcmvk+r1IKfvaNX0MJ5eo5+fgXB8jwq6PpBYW2PU/vptctJ8UvPb0t0bnLpepOnzNkhUacTOezAf988k35+gw9Vrh6rXG4x7cZ65qbQOP+Xh0sx3YElyZKUBJzl5CMjNzT5ANc/QdpCD8LOOiPF4xpcHqKih74NGXc8hQv1AQJBAMNm1i6AC/oJM7XDnXPyswNVyjjIG+wi9xBgMGlt8JLH3c7/HblyAPHS/sFnljroVsHOyvoi45aZlIluhZsh6tUCQQC+QygmlnViImHq+MgL6bWaTLjKyTpH6k7zGQxvOxqJlioeWh73wxHAq/depKi8ElMrkEhMBA05ReCJl4Nb8xzRAkA28/HiS/KSVAot4SCj3iqIEpV3mJd5tm+jNFoJHHke3oS71TWH1M79M2if/cDbOkJD6SNea3d0ACcs6185vLUtAkBtrWLw05z5JB7UB/Oxwli4iO+hnlxlZnF6e38Kg8SpeZHwCz18z8tlCPzBZyQJvnqJS1QR1egVku18A4Zqs/txAkAg5kjdDw0v9QoQMr4oHZSuHxaG9I91SRkCPspN8urIg1Wu7cdTKfPdaAtSoU/xp/qpzfX+CPkhv7DoGlBLeo8e";

//    private Package pack;

    @BeforeMethod
    public void setUp() {

        SignedTransaction signedTx2 = new SignedTransaction();
        CoreTransaction coreTx2 = new CoreTransaction();

        coreTx2.setTxId("pending-tx-test-2");
        coreTx2.setActionList(initPolicy());
        coreTx2.setPolicyId("000000");
        coreTx2.setLockTime(new Date());
        coreTx2.setBizModel(new JSONObject());
        coreTx2.setSender("TRUST-NODE31");
        coreTx2.setVersion(VersionEnum.V1.getCode());
        coreTx2.setSendTime(new Date());

        signedTx2.setCoreTx(coreTx2);
        signedTx2.setSignatureList(buildSignInfoList(coreTx2));

        SignedTransaction signedTx1 = new SignedTransaction();
        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("pending-tx-test-1");
        coreTx1.setActionList(initPolicy());
        coreTx1.setPolicyId("000000");
        coreTx1.setLockTime(new Date());
        coreTx1.setBizModel(new JSONObject());
        coreTx1.setSender("TRUST-NODE31");
        coreTx1.setVersion(VersionEnum.V1.getCode());
        coreTx1.setSendTime(new Date());

        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(buildSignInfoList(coreTx1));
        signedTxList.add(signedTx1);


        signedTxList.add(signedTx2);
    }

    private List<Action> initPolicy() {
        RegisterPolicy registerPolicy = new RegisterPolicy();

        List<String> rsIds = new ArrayList<>();
        rsIds.add("TRUST-TEST1");
        registerPolicy.setRsIds(rsIds);
        registerPolicy.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy.setIndex(0);

        List<Action> registerPolicies = new ArrayList<>();
        registerPolicies.add(registerPolicy);
        registerPolicy.setPolicyId("test-policy-1");
        registerPolicy.setPolicyName("测试注册policy-1");
        registerPolicy.setDecisionType(DecisionTypeEnum.FULL_VOTE);
        registerPolicy.setContractAddr(null);


        return registerPolicies;
    }

    private List<SignInfo> buildSignInfoList(CoreTransaction coreTx) {
        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx), priKey1);
        String sign2 = SignUtils.sign(JSON.toJSONString(coreTx), priKey2);
        List<SignInfo> signList = new ArrayList<>();


        SignInfo signInfo2 = new SignInfo();
        signInfo2.setOwner("NODE-TEST2");
        signInfo2.setSign(sign2);
        signList.add(signInfo2);

        SignInfo signInfo1 = new SignInfo();
        signInfo1.setOwner("NODE-TEST1");
        signInfo1.setSign(sign1);
        signList.add(signInfo1);

        return signList;
    }

    @Test public void create() {
        Package pack;
        //test signedTxList is null
        pack = packageService.create(null, null);
        assertEquals(pack, null);

        //test packHeight is null
        pack = packageService.create(signedTxList, null);
        assertEquals(new Long(2L), pack.getHeight());
//        System.out.println(pack);
    }

    @Test public void receive() {
//        Package pack = packageService.create(signedTxList, null);
        Package pack = packageRepository.load(2L);
        packageService.receive(pack);
//        Package pack = packageRepository.load(2L);
//        System.out.println(JSON.toJSONString(pack));
//        packageService.receive(pack);
    }

    @Test public void persisted() {

    }

    @Test public void createPackContext() {
        Package pack = packageRepository.load(2L);
        PackContext packContext = packageService.createPackContext(pack);
        assertEquals(packContext.getRsPubKeyMap().size(), 2);
    }

    @Test public void process() {

    }

    @Test public void getSign() {

    }
}