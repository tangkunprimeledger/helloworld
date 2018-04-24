package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.utxo.UTXOStatusEnum;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.*;

public class TxOutRepositoryTest extends BaseTest{
    @Autowired
    private TxOutRepository txOutRepository;
    @Test
    public void testQueryTxOut() throws Exception {
        System.out.println("queryTxOut :" + txOutRepository.queryTxOut("123123", 0, 0));
    }

    @Test
    public void testBatchInsert() throws Exception {
        List<TxOutPO> txOutPOList = new ArrayList<>();
        TxOutPO txOutPO = new TxOutPO();
        txOutPO.setTxId("123123"+ new Date());
        txOutPO.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        txOutPO.setActionIndex(0);
        txOutPO.setIndex(0);
        txOutPO.setContract("12321"+ new Date());
        txOutPO.setState("12321");
        txOutPO.setStateClass("afdaf");
        txOutPO.setIdentity("12312312");

        TxOutPO txOutPO1 = new TxOutPO();
        txOutPO1.setTxId("123123" + new Date());
        txOutPO1.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        txOutPO1.setActionIndex(0);
        txOutPO1.setIndex(1);
        txOutPO1.setContract("12321");
        txOutPO1.setState("12321");
        txOutPO1.setStateClass("afdaf");
        txOutPO1.setIdentity("12312312");

        TxOutPO txOutPO2 = new TxOutPO();
        txOutPO2.setTxId("123123" + new Date());
        txOutPO2.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        txOutPO2.setActionIndex(0);
        txOutPO2.setIndex(2);
        txOutPO2.setContract("12321");
        txOutPO2.setState("12321");
        txOutPO2.setStateClass("afdaf");
        txOutPO2.setIdentity("12312312");

        txOutPOList.add(txOutPO);
        txOutPOList.add(txOutPO1);
        txOutPOList.add(txOutPO2);
        System.out.println("testBatchInsert:"+txOutRepository.batchInsert(txOutPOList));
    }

    @Test
    public void testBatchUpdate() throws Exception {

        List<TxOutPO> txOutPOList = new ArrayList<>();
        TxOutPO txOutPO = new TxOutPO();
        txOutPO.setTxId("123123");
        txOutPO.setSTxId("123123" + new Date());
        txOutPO.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        txOutPO.setActionIndex(0);
        txOutPO.setIndex(0);

        TxOutPO txOutPO1 = new TxOutPO();
        txOutPO1.setTxId("123123");
        txOutPO1.setSTxId("123123" + new Date());
        txOutPO1.setStatus(UTXOStatusEnum.SPENT.getCode());
        txOutPO1.setActionIndex(0);
        txOutPO1.setIndex(1);

        TxOutPO txOutPO2 = new TxOutPO();
        txOutPO2.setTxId("123123");
        txOutPO2.setSTxId("123123" + new Date());
        txOutPO2.setStatus(UTXOStatusEnum.SPENT.getCode());
        txOutPO2.setActionIndex(0);
        txOutPO2.setIndex(2);

        txOutPOList.add(txOutPO);
        txOutPOList.add(txOutPO1);
        txOutPOList.add(txOutPO2);

        System.out.println("testBatchUpdate:"+txOutRepository.batchInsert(txOutPOList));

    }

}