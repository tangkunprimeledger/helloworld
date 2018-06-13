package com.higgs.trust.slave.core.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.CancelRS;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.testng.Assert.assertEquals;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/18
 *
 */
public class PackageSchedulerTest extends BaseTest{

    @Autowired
    private PackageScheduler packageScheduler;

    @Autowired private Deque<SignedTransaction> pendingTxQueue;

//    @Autowired private BlockingQueue<Package> pendingPack;

    private static final List<SignedTransaction> signedTxList = new ArrayList<>();

    @Autowired private PackageService packageService;


    //98
    private String priKey1 = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMOi9ploA0ciLOtlCFV/VPWlmJnlrnOwf1HztlAC70ztVZM2aNwOj8DKSIbrWqPb7ipuCooE7Fjcokz1xAAafE509DfqGrDKG7u6FDVaKtHHf1wzfFzQ3bZl6GfQYg1+MtfmgtSEOdlPYDseJv9q0hy631cWERLMZ5sYyDWgTG3LAgMBAAECgYEAjyxCQH6od3SBXzwvXzKyoJuyFF7MzwnKA9XSBLhBHRFL4VKeZv2ZIsRnU6YGTYj0wcSKnfuAZIADn0L1UAYSFabmfDD4fWN0SEATJ5x8RS84IaFtjABUL0/LEEnXk+zuv2lfDnO8ZfnYCMkq5sZoTsdHvJguPvwNUKHiL4ylt7ECQQD8//Ko9hyh0CU9CShdOGo8uQMdk7nPOgmaZCe4n4a5txypzaEpJlxVsCrsdjorzrSHJtlGWhkwIrLfajmvFKwFAkEAxfTfiL95CEAU5rrh3M4DxnESE7mHzo7UEaD3S1p6MIMNfQBlpxbPR3ouvWh6mUa9RjUMpRheXFAcwrgxIcWrjwJAf0QlBfdfqcoxoUVlB5ekYU1UEuHH20U/pWlyTb35oJxLD+O/iRqh4fzPasmCM500VLQnIeuatgsOXbJqt/a3HQJAU4nwX5w2LoYki+5n7K/AJhmHgG12Y3QhCMN+OP2YvSpBIfRt8aKNnONyIBqcSlzw+VTf3jg3cguY54+lpFPv/wJBAL5rG4paENy4+NRspa2GPIR6zJATqDOFvxI9LZZmE1LNUhfwF67Gs9RiJAPmNgSxMhcGjLx9HVT+33MQOonHsxU=";

    //100
    private String priKey2 = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJE5lYYWx1AE5E0XaxmQvg2putYOrya+Azd+QCHVA0rE8vUiXAuFia7TgjGQjhFO66SrcOJ3W2xSeHw6F1ApvZ8kfLB3ZsZT1e54QaWKU0ae0fcmk9dgrkpaniLCbd3PP8A+UUjwNeexIDjThYSzzatNGg06qdxgSCMc9bX7jwPlAgMBAAECgYA5Vtcmvk+r1IKfvaNX0MJ5eo5+fgXB8jwq6PpBYW2PU/vptctJ8UvPb0t0bnLpepOnzNkhUacTOezAf988k35+gw9Vrh6rXG4x7cZ65qbQOP+Xh0sx3YElyZKUBJzl5CMjNzT5ANc/QdpCD8LOOiPF4xpcHqKih74NGXc8hQv1AQJBAMNm1i6AC/oJM7XDnXPyswNVyjjIG+wi9xBgMGlt8JLH3c7/HblyAPHS/sFnljroVsHOyvoi45aZlIluhZsh6tUCQQC+QygmlnViImHq+MgL6bWaTLjKyTpH6k7zGQxvOxqJlioeWh73wxHAq/depKi8ElMrkEhMBA05ReCJl4Nb8xzRAkA28/HiS/KSVAot4SCj3iqIEpV3mJd5tm+jNFoJHHke3oS71TWH1M79M2if/cDbOkJD6SNea3d0ACcs6185vLUtAkBtrWLw05z5JB7UB/Oxwli4iO+hnlxlZnF6e38Kg8SpeZHwCz18z8tlCPzBZyQJvnqJS1QR1egVku18A4Zqs/txAkAg5kjdDw0v9QoQMr4oHZSuHxaG9I91SRkCPspN8urIg1Wu7cdTKfPdaAtSoU/xp/qpzfX+CPkhv7DoGlBLeo8e";

    //99
    private String priKey3 = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIrjgTKcxdzf4pJf97wOrG6NixxPCIxQ+BLsIHLBgacwuKh5gDYhxRcuq2XCE/HX/Xi4RhL18x7CQ95OU6q4oPFfpevX6zxuBqWzQ7F0FH8KMAc82cbUZmXGV803wgjIVqBtwYkPznftx1v2deRnQVigLHoZlJtr4mgyPFkFF5WJAgMBAAECgYBZf/Gpo8z9YGio2p7R2MLVGvEh9OwVP7gcuXzPdlMOYh8cse8k8u4G2lQo5r/jgMQeHuFJJqTclWMWxnKz+PX8nsWvrjl0VFB9kZGWvcAHZXky0DQ8yActFYuxMZZT8eyFjHJ6kynjLGKpsvyYzDHH7PFPFUbDAMr4FIqt2zGngQJBAN5v9dMklCVGTHZIs5bm3+MINTZmaGa/TYFDj+p7R0oosThx0+uVFTtEhurzMN4V8RNieBleKjIEQNMp6y4lxO0CQQCf2FQSyK26oncARWEvCyjkIhisKRgidoVzJMiX3PTxiz14xDI4zpycEkoG3Y5idKdsoUwC5JUprt3n58nMFbuNAkAwLA0P0f8nZ2cNwsbp6kwYTeHAS0NW5R0y3l/fhx00SUXAFJ6xiVLUyA1z+oDdx+CyswORctwugs9LK+vbzaAtAkAkienmBVOZNywmtrVZcJ6fT5/+MsKeliM5R+5GsK6ZTG/33DlyvOAV2SRs31Z98RaYgWKDwsbKKXv2WAjMCye9AkAMfoZc2AKhgY/URsALVPOggcRybpZiNAo6iZSMSu7JG0Vkc0l2JPje2MTN6NpulikPfghoSDr0fpLbkR4CiVIQ";

    //101
    private String priKey4 = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIiVJuf1C5CXeDl/g7mcZ4daNsMkdO33OU4LU5CWtXcIMXONRM0/jcHw171TM9PaV+c31TRfZKkhurm5cbzN7+wOT+745AX0BxM2oGXKKNIuyt9gtO91d1O87K39q3Vjf65vzjjIUjT9tLzTDotCGSAs3MHolZqZuyTMNbvRQ2RFAgMBAAECgYBpCMKtuP2WyVU0aoKDV+sp5M+eTEf7xJZ2FTHDTJLqogtdgHM7oDdEwRE30GdX8V6OOpqkNwPzKTQupZT5DCoHaLlKSNRCpQMfANmb84//0B6sLj8nTwcL+Ap51l/XOkhlJ/Pe/uElDaBYP05/SV7yD25xYJTMa7KUxn0JGsW3IQJBAOni5qrZ2/Xfl7VMbq3eGqDYkB+fJPpAXcfMg94+mKYoyZEptr/b78a3yvGXo4Fu8He3KXVmEjPMGL8JTXgURN0CQQCVfxCDPUA6s+D5BcH7x0ivMwaJedk4k0uaGW9Tfxl9SMFxmLQaRRdg/Pc/FJBV7cWDrjLUfK4CVJ0Zr2whLxKJAkAH8MUmDcly+olbBWJIkt7SkSereyq1OFQ2wVHEzNk/4uic/g6PSvdEBt9j/mL1tP+DAPvuWW3KxmYcrkMsDXwdAkADcTKLIwzojR2//h7yUitdRVqALdsPj2ytNqq2jHWkydkSaYa0GCUqqYhz3mBYaiojFw66j8hd617WqZBezzupAkEAjIkafm75nhYx9ObTkZU3WDyeSACr7NH/bkaKtWibDRhfuELgdW9LUrXXnqImLgFu8VvbqF/zBhkaFWAcfy9mSA==";

    @BeforeMethod
    public void setUp() {
//        createPolicyTx();
        createRegisterRsTx();
//        createCancelRsTx();
        signedTxList.forEach(signedTx->{
            pendingTxQueue.offerLast(signedTx);
        });
    }

    private void createCancelRsTx() {
        SignedTransaction signedTx1 = new SignedTransaction();
        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("pending-tx-test-13");

        List<Action> cancelRSList = new ArrayList<>();
        CancelRS cancelRS = new CancelRS();
        cancelRS.setRsId("TRUST-NODE-TEST1");
        cancelRS.setIndex(0);
        cancelRS.setType(ActionTypeEnum.RS_CANCEL);
        cancelRSList.add(cancelRS);

        coreTx1.setActionList(cancelRSList);
        coreTx1.setPolicyId(InitPolicyEnum.CANCEL_RS.getPolicyId());
        coreTx1.setLockTime(new Date());
        coreTx1.setBizModel(new JSONObject());
        coreTx1.setSender("TRUST-NODE-TEST1");
        coreTx1.setVersion(VersionEnum.V1.getCode());
        coreTx1.setSendTime(new Date());

        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(buildSignInfoList(coreTx1));
        signedTxList.add(signedTx1);
    }
    private void createRegisterRsTx() {
        SignedTransaction signedTx1 = new SignedTransaction();
        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("pending-tx-test-14");

        List<Action> registerRSList = new ArrayList<>();
        RegisterRS registerRS = new RegisterRS();
        registerRS.setType(ActionTypeEnum.REGISTER_RS);
        registerRS.setIndex(0);
        registerRS.setDesc("TRUST-NODE-TEST1");
        registerRS.setRsId("TRUST-NODE-TEST1");

        registerRSList.add(registerRS);
        coreTx1.setActionList(registerRSList);
        coreTx1.setPolicyId("000002");
        coreTx1.setLockTime(new Date());
        coreTx1.setBizModel(new JSONObject());
        coreTx1.setSender("TRUST-NODE-TEST1");
        coreTx1.setVersion(VersionEnum.V1.getCode());
        coreTx1.setSendTime(new Date());

        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(buildSignInfoList(coreTx1));
        signedTxList.add(signedTx1);


//        SignedTransaction signedTx2 = new SignedTransaction();
//        CoreTransaction coreTx2 = new CoreTransaction();
//
//        coreTx2.setTxId("pending-tx-test-9");
//
//        List<Action> registerRSList1 = new ArrayList<>();
//        RegisterRS registerRS1 = new RegisterRS();
//        registerRS1.setType(ActionTypeEnum.REGISTER_RS);
//        registerRS1.setIndex(0);
//        registerRS1.setDesc("TRUST-NODE-TEST2");
//        registerRS1.setRsId("TRUST-NODE-TEST2");
//        registerRSList1.add(registerRS1);
//
//        coreTx2.setActionList(registerRSList1);
//        coreTx2.setPolicyId("000002");
//        coreTx2.setLockTime(new Date());
//        coreTx2.setBizModel(new JSONObject());
//        coreTx2.setSender("TRUST-NODE-TEST2");
//        coreTx2.setVersion(VersionEnum.V1.getCode());
//        coreTx2.setSendTime(new Date());
//
//        signedTx2.setCoreTx(coreTx2);
//        signedTx2.setSignatureList(buildSignInfoList(coreTx2));
//        signedTxList.add(signedTx2);
    }

    public void createPolicyTx() {
        SignedTransaction signedTx1 = new SignedTransaction();
        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("pending-tx-test-1");

        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId("test-policy-1");
        registerPolicy.setPolicyName("测试注册policy-1");
        registerPolicy.setDecisionType(DecisionTypeEnum.FULL_VOTE);
        registerPolicy.setContractAddr(null);

        List<String> rsIds = new ArrayList<>();
        rsIds.add("TRUST-TEST98");
        registerPolicy.setRsIds(rsIds);
        registerPolicy.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy.setIndex(0);
        List<Action> registerPolicies = new ArrayList<>();
        registerPolicies.add(registerPolicy);

        coreTx1.setActionList(registerPolicies);
        coreTx1.setPolicyId("000001");
        coreTx1.setLockTime(new Date());
        coreTx1.setBizModel(new JSONObject());
        coreTx1.setSender("TRUST-NODE98");
        coreTx1.setVersion(VersionEnum.V1.getCode());
        coreTx1.setSendTime(new Date());

        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(buildSignInfoList(coreTx1));
        signedTxList.add(signedTx1);

        SignedTransaction signedTx2 = new SignedTransaction();
        CoreTransaction coreTx2 = new CoreTransaction();

        coreTx2.setTxId("pending-tx-test-2");

        RegisterPolicy registerPolicy1 = new RegisterPolicy();
        registerPolicy1.setPolicyId("test-policy-2");
        registerPolicy1.setPolicyName("测试注册policy-2");
        registerPolicy1.setDecisionType(DecisionTypeEnum.FULL_VOTE);
        registerPolicy1.setContractAddr(null);

        List<String> rsIds1 = new ArrayList<>();
        rsIds1.add("TRUST-NODE98");
        rsIds1.add("TRUST-NODE100");
        registerPolicy1.setRsIds(rsIds1);
        registerPolicy1.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy1.setIndex(0);

        List<Action> registerPolicies1 = new ArrayList<>();
        registerPolicies1.add(registerPolicy1);

        coreTx2.setActionList(registerPolicies1);
        coreTx2.setPolicyId("000001");
        coreTx2.setLockTime(new Date());
        coreTx2.setBizModel(new JSONObject());
        coreTx2.setSender("TRUST-NODE98");
        coreTx2.setVersion(VersionEnum.V1.getCode());
        coreTx2.setSendTime(new Date());

        signedTx2.setCoreTx(coreTx2);
        signedTx2.setSignatureList(buildSignInfoList(coreTx2));
        signedTxList.add(signedTx2);
    }

    private List<SignInfo> buildSignInfoList(CoreTransaction coreTx) {
        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx), priKey1);
        List<SignInfo> signList = new ArrayList<>();
        SignInfo signInfo1 = new SignInfo();
        signInfo1.setOwner("TRUST-NODE98");
        signInfo1.setSign(sign1);
        signList.add(signInfo1);

        String sign2 = SignUtils.sign(JSON.toJSONString(coreTx), priKey2);
        SignInfo signInfo2 = new SignInfo();
        signInfo2.setOwner("TRUST-NODE100");
        signInfo2.setSign(sign2);
        signList.add(signInfo2);

        String sign3 = SignUtils.sign(JSON.toJSONString(coreTx), priKey3);
        SignInfo signInfo3 = new SignInfo();
        signInfo3.setOwner("TRUST-NODE-TEST1");
        signInfo3.setSign(sign3);
        signList.add(signInfo3);

        String sign4 = SignUtils.sign(JSON.toJSONString(coreTx), priKey4);
        SignInfo signInfo4 = new SignInfo();
        signInfo4.setOwner("TRUST-NODE-TEST2");
        signInfo4.setSign(sign4);
        signList.add(signInfo4);

        return signList;
    }

    private List<Action> initPolicy() {
        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId("test-policy-1");
        registerPolicy.setPolicyName("测试注册policy-1");
        registerPolicy.setDecisionType(DecisionTypeEnum.FULL_VOTE);
        registerPolicy.setContractAddr(null);

        List<String> rsIds = new ArrayList<>();
        rsIds.add("TRUST-NODE98");
        registerPolicy.setRsIds(rsIds);
        registerPolicy.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy.setIndex(0);

        List<Action> registerPolicies = new ArrayList<>();
        registerPolicies.add(registerPolicy);
        return registerPolicies;
    }

    @Test public void createPackage() {
        Package pack = packageScheduler.createPackage();
        packageService.receive(pack);
    }

    @Test public void submitPackage() {
        packageScheduler.createPackage();
        packageScheduler.submitPackage();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test public void processPackage() {
        packageScheduler.processPackage();
    }
}