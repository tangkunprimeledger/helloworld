package com.higgs.trust.slave.core.service.snapshot.agent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.utxo.UTXOStatusEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class UTXOSnapshotAgentTest extends  BaseTest{
    @Autowired
    private UTXOSnapshotAgent utxoSnapshotAgent;
    @Autowired
    private SnapshotService snapshotService;
    @Test
    public void testQueryTxOut() throws Exception {
        System.out.println("queryTxOut :" + utxoSnapshotAgent.queryUTXO("123", 0, 0));


    }

    @Test
    public  void  testJson(){
      //  System.setProperty("spring.config.location", "classpath:test-application.json");
        //JSON auto detect class type
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //JSON不做循环引用检测
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        //JSON输出NULL属性
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        //toJSONString的时候对一级key进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.SortField.getMask();
        //toJSONString的时候对嵌套结果进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.MapSortField.getMask();
        //toJSONString的时候记录Class的name
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteClassName.getMask();
        String jsonStr ="[{\"coreTx\":{\"actionList\":[{\"chainOwner\":\"TRUST\",\"dataOwner\":\"TRUST-NODE97\",\"identity\":\"lingChao1524754278113\",\"index\":0,\"type\":\"CREATE_DATA_IDENTITY\"}],\"bizModel\":{\"data\":{\"$ref\":\"$[0].coreTx.actionList[0]\"}},\"lockTime\":1524754278127,\"policyId\":\"test-policy-1\",\"sender\":\"TRUST-NODE97\",\"txId\":\"tx_id_CREATE_DATA_IDENTITY_0_1524754278126\",\"version\":\"1.0.0\"},\"signatureList\":[\"drvx6HPqQoCt71Cwshtx9zA1YBXf2oyMBZmLnw+XbVG/5ayQ01EXeFc6ydy1CVeAzbH2duWVoCZ6jatH28D6gL8vzOsK32+g2vGdAfwCCeQ0DZq9qwAaDAt/fx/bQPDOk9Q+fjq1is6aYJQmh5m+GaAtriVqOcMB1sJQ+lY/IVs=\",\"D4f2RrSavGRY5bnUScLQ8PzNrVtyHjbE/E+3/aRDtNL0cKQhS6aBBdzW8qhGsGk9OncRYPxvVxqV40JvgW166LWRvMkp6nXPMFHXNzPhAYBirQv0XREy8tYza9hWtnUNTsMUQvNNlZIK1CHENrJxdBpdGmkLVgOpmSiiDWQj1hs=\"]}]";
              //  JSONObject json = JSON.parseArray(jsonStr);
        System.out.println();
    }

    @Test
    public void testBatchInsertTxOut() throws Exception {
        JSONObject state = new JSONObject();
        state.put("amount", "100");
        List<TxOutPO> txOutPOList = new ArrayList<>();
        TxOutPO txOutPO = new TxOutPO();
        txOutPO.setTxId("123123"+ new Date());
        txOutPO.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        txOutPO.setActionIndex(0);
        txOutPO.setIndex(0);
        txOutPO.setContractAddress("12321"+ new Date());
        txOutPO.setState(state.toJSONString());
        txOutPO.setStateClass("afdaf");
        txOutPO.setIdentity("12312312");

        TxOutPO txOutPO1 = new TxOutPO();
        txOutPO1.setTxId("123123" + new Date());
        txOutPO1.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        txOutPO1.setActionIndex(0);
        txOutPO1.setIndex(1);
        txOutPO1.setContractAddress("12321");
        txOutPO1.setState(state.toJSONString());
        txOutPO1.setStateClass("afdaf");
        txOutPO1.setIdentity("12312312");

        TxOutPO txOutPO2 = new TxOutPO();
        txOutPO2.setTxId("123123" + new Date());
        txOutPO2.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        txOutPO2.setActionIndex(0);
        txOutPO2.setIndex(2);
        txOutPO2.setContractAddress("12321");
        txOutPO2.setState(state.toJSONString());
        txOutPO2.setStateClass("afdaf");
        txOutPO2.setIdentity("12312312");

        txOutPOList.add(txOutPO);
        txOutPOList.add(txOutPO1);
        txOutPOList.add(txOutPO2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                snapshotService.startTransaction();
            }
        }).start();
        snapshotService.startTransaction();
        utxoSnapshotAgent.batchInsertTxOut(txOutPOList);
        System.out.println("queryTxOut :" + utxoSnapshotAgent.queryUTXO(txOutPO.getTxId(), txOutPO.getIndex(), txOutPO.getActionIndex()));
        snapshotService.commit();
        System.out.println("queryTxOut :" + utxoSnapshotAgent.queryUTXO(txOutPO.getTxId(), txOutPO.getIndex(), txOutPO.getActionIndex()));
        snapshotService.destroy();
    }

    @Test
    public void testBachUpdateTxOut() throws Exception {
        List<TxOutPO> txOutPOList = new ArrayList<>();
        TxOutPO txOutPO = new TxOutPO();
        txOutPO.setTxId("123123"+ new Date());
        txOutPO.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        txOutPO.setActionIndex(0);
        txOutPO.setIndex(0);
        txOutPO.setContractAddress("12321"+ new Date());
        txOutPO.setState("12321");
        txOutPO.setStateClass("afdaf");
        txOutPO.setIdentity("12312312");

        TxOutPO txOutPO1 = new TxOutPO();
        txOutPO1.setTxId("123123" + new Date());
        txOutPO1.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        txOutPO1.setActionIndex(0);
        txOutPO1.setIndex(1);
        txOutPO1.setContractAddress("12321");
        txOutPO1.setState("12321");
        txOutPO1.setStateClass("afdaf");
        txOutPO1.setIdentity("12312312");

        TxOutPO txOutPO2 = new TxOutPO();
        txOutPO2.setTxId("123123" + new Date());
        txOutPO2.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        txOutPO2.setActionIndex(0);
        txOutPO2.setIndex(2);
        txOutPO2.setContractAddress("12321");
        txOutPO2.setState("12321");
        txOutPO2.setStateClass("afdaf");
        txOutPO2.setIdentity("12312312");

        txOutPOList.add(txOutPO);
        txOutPOList.add(txOutPO1);
        txOutPOList.add(txOutPO2);
        snapshotService.startTransaction();
        utxoSnapshotAgent.bachUpdateTxOut(txOutPOList);
        System.out.println("queryTxOut :" + utxoSnapshotAgent.queryUTXO(txOutPO.getTxId(), txOutPO.getIndex(), txOutPO.getActionIndex()));
        snapshotService.commit();
        System.out.println("queryTxOut :" + utxoSnapshotAgent.queryUTXO(txOutPO.getTxId(), txOutPO.getIndex(), txOutPO.getActionIndex()));
        snapshotService.destroy();
    }

    @Test
    public void testQuery() throws Exception {
    }

}