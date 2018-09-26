package com.higgs.trust.slave.dao.mysql.transaction;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import com.higgs.trust.slave.core.service.block.hash.TxRootHashBuilder;
import com.higgs.trust.slave.dao.po.transaction.TransactionPO;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-05-10
 */
public class TransactionRepositoryTest extends IntegrateBaseTest {

    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    TransactionDao transactionDao;

    @Autowired
    TxRootHashBuilder txRootHashBuilder;

    @Test
    public void test() {
        transactionRepository.isExist("12345");
    }

    @Test
    public void testAdd() {
        Long height = System.currentTimeMillis();
        UTXOAction utxoAction = new UTXOAction();

        List<TxIn> inputList = org.testng.collections.Lists.newArrayList();
        TxIn txIn = new TxIn();
        txIn.setTxId("b76c4bac8007792e0b565d121c7213cd0f22d84e2d07b5e14c4e47675be33582");
        txIn.setActionIndex(0);
        txIn.setIndex(0);
        inputList.add(txIn);


        List<TxOut> outputList = Lists.newArrayList();
        TxOut txOut = new TxOut();
        txOut.setIdentity("8df8999bd048bef78f19ce7e9939b33a7e074b6ac6b49e15a7f50026db05b0d4");
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        JSONObject state = new JSONObject();
        state.put("@type", "com.alibaba.fastjson.JSONObject");
        state.put("currency", "BUC");
        state.put("amount", new BigDecimal("1889989979.0129460000"));
        txOut.setState(state);

        TxOut txOut1 = new TxOut();
        txOut1.setIdentity("b00e88e3c85e6759f8032171a9c26144e23b31541576237c6cf7a981e6629404");
        txOut1.setIndex(1);
        txOut1.setActionIndex(0);
        JSONObject state1 = new JSONObject();
        state1.put("@type", "com.alibaba.fastjson.JSONObject");
        state1.put("currency", "BUC");
        state1.put("amount", new BigDecimal("9.9998000000"));
        txOut1.setState(state1);

        outputList.add(txOut);
        outputList.add(txOut1);

        utxoAction.setIndex(0);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(outputList);
        utxoAction.setStateClass("2342");
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setContractAddress("1234567890");
        List<Action> actionList = Lists.newArrayList(utxoAction);
        CoreTransaction coreTx = new CoreTransaction();

        coreTx.setActionList(actionList);
        coreTx.setSender("lingchao");
        coreTx.setPolicyId("123123");
        coreTx.setSendTime(new Date());
        coreTx.setTxId(height+"");
        coreTx.setVersion("1.0.0");
        JSONObject bizModel = new JSONObject();
        bizModel.put("OO", "ling");
        bizModel.put("OO11", "ling");
        coreTx.setBizModel(bizModel);

        TransactionPO po = BeanConvertor.convertBean(coreTx, TransactionPO.class);
        if (coreTx.getBizModel() != null) {
            po.setBizModel(coreTx.getBizModel().toJSONString());
        }
        po.setBlockHeight(height);
        po.setBlockTime(new Date());
        SignInfo signInfo = new SignInfo();

        signInfo.setOwner("lingchao");
        signInfo.setSign("123456");
        po.setSignDatas(JSONObject.toJSONString(Lists.newArrayList(signInfo)));
        po.setActionDatas(JSON.toJSONString(coreTx.getActionList()));
        po.setSendTime(coreTx.getSendTime());

        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.setCoreTx(coreTx);
        signedTransaction.setSignatureList(Lists.newArrayList(signInfo));
        System.out.println("" + transactionDao.add(po));

        List<SignedTransaction> list = transactionRepository.queryTransactions(height);
        System.out.println("origin tx_rootHash=" + txRootHashBuilder.buildTxs(Lists.newArrayList(signedTransaction)));
        System.out.println("db     tx_rootHash="+ txRootHashBuilder.buildTxs(list));
        System.out.println("origin bizModel="+ coreTx.getBizModel());
        System.out.println("db     bizModel="+ list.get(0).getCoreTx().getBizModel());
        System.out.println("origin actions="+ coreTx.getActionList());
        System.out.println("db     actions="+ list.get(0).getCoreTx().getActionList());
    }
}
