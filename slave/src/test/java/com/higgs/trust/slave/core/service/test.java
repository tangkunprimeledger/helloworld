package com.higgs.trust.slave.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.core.service.action.account.TestDataMaker;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


/**
 * test
 *
 * @author lingchao
 * @create 2018年05月03日15:20
 */
@Slf4j
public class test{
    private  static String pubk1 ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCLDDNiZKp0AAjfkVKkReFJFufF546YwPkT+JpwOLg/Sgo5SXk8b2NMhw6rLyGol6+xntEAdVQCqtMYrdgVh31VP15Ttbaivnmu2MaKHqIJbzpHXYiYmE2VDYyjOQIbbRvedD6c65nlHJfWBhGfw8CinnvvLamg1nSBBwZ6ZTuplQIDAQAB";
    private  static String pubk2 ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCROZWGFsdQBORNF2sZkL4NqbrWDq8mvgM3fkAh1QNKxPL1IlwLhYmu04IxkI4RTuukq3Did1tsUnh8OhdQKb2fJHywd2bGU9XueEGlilNGntH3JpPXYK5KWp4iwm3dzz/APlFI8DXnsSA404WEs82rTRoNOqncYEgjHPW1+48D5QIDAQAB";

 public  static  void main(String[] args) throws Exception {
     List<SignedTransaction> signedTransactions = makeRegisterPolicyTxs();
     System.out.println("sing1+pubk1:" + SignUtils.verify(JSON.toJSONString(signedTransactions.get(0).getCoreTx()), signedTransactions.get(0).getSignatureList().get(0), pubk1));
     System.out.println("sing2+pubk2:" +  SignUtils.verify(JSON.toJSONString(signedTransactions.get(0).getCoreTx()), signedTransactions.get(0).getSignatureList().get(1), pubk2));
 }

    private static List<SignedTransaction> makeOpenAccountTxs() throws Exception {
        List<SignedTransaction> txs = new ArrayList<>();
        List<Action> actions = new ArrayList<>();
        Action action = TestDataMaker.makeOpenAccountAction("ling", FundDirectionEnum.CREDIT);
        actions.add(action);

        JSONObject bizModel = new JSONObject();
        bizModel.put("data",action);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, "test-policy-1",bizModel);
        coreTransaction.setSender("TRUST-NODE97");
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);

        txs.add(tx);
        return txs;
    }

    private static List<SignedTransaction> makeRegisterPolicyTxs() throws Exception {
        List<SignedTransaction> txs = new ArrayList<>();
        List<Action> actions = new ArrayList<>();
        Action action = TestDataMaker.createRegisterPolicyAction();
        actions.add(action);

        JSONObject bizModel = new JSONObject();
        bizModel.put("data",action);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, "000000",bizModel);
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);

        txs.add(tx);
        return txs;
    }


}
