package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.utxo.UTXOStatusEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class UTXOSnapshotAgentTest extends BaseTest {
    @Autowired
    private UTXOSnapshotAgent utxoSnapshotAgent;
    @Autowired
    private SnapshotService snapshotService;
    @Test
    public void testQueryTxOut() throws Exception {
        System.out.println("queryTxOut :" + utxoSnapshotAgent.queryUTXO("123", 0, 0));


    }

    @Test
    public void testBatchInsertTxOut() throws Exception {
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