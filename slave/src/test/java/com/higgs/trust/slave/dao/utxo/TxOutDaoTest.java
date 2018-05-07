package com.higgs.trust.slave.dao.utxo;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.utxo.UTXOStatusEnum;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * TxOutDao test
 *
 * @author lingchao
 * @create 2018年03月29日15:51
 */
public class TxOutDaoTest extends BaseTest {

    @Autowired private TxOutDao txOutDao;

    @Test
    public void batchInsertTest() {
        long count = 0L;
        do {
            List<TxOutPO> txOutPOList = new ArrayList<>();
            for(int i=0;i<1;i++){
                TxOutPO txOutPO = new TxOutPO();
                txOutPO.setTxId("123123" + System.currentTimeMillis()+new Random());
                txOutPO.setStatus(UTXOStatusEnum.UNSPENT.getCode());
                txOutPO.setActionIndex(0);
                txOutPO.setIndex(i);
                txOutPO.setContract("12321" + new Date());
                txOutPO.setState("12321");
                txOutPO.setStateClass("afdaf");
                txOutPO.setIdentity("12312312");
                txOutPOList.add(txOutPO);
            }
            TxOutPO txOutPO = new TxOutPO();
            txOutPO.setTxId("123123");
            txOutPO.setStatus(UTXOStatusEnum.UNSPENT.getCode());
            txOutPO.setActionIndex(0);
            txOutPO.setIndex(0);
            txOutPO.setContract("12321" + new Date());
            txOutPO.setState("12321");
            txOutPO.setStateClass("afdaf");
            txOutPO.setIdentity("12312312");
            txOutPOList.add(txOutPO);
            count++;
            System.out.println("count ："+ count +"  batchInsert :" + txOutDao.batchInsert(txOutPOList)+"  size= "+txOutPOList.size());
        }while (count!=1L);
    }

    @Test public void batchUpdateTest() {

        List<TxOutPO> txOutPOList = new ArrayList<>();
        TxOutPO txOutPO = new TxOutPO();
        txOutPO.setTxId("123123");
        txOutPO.setSTxId("123123" + new Date());
        txOutPO.setStatus(UTXOStatusEnum.SPENT.getCode());
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

        System.out.println("batchUpdate :" + txOutDao.batchUpdate(txOutPOList));
    }

    @Test public void queryTxOutTest() {
        System.out.println("queryTxOut :" + txOutDao.queryTxOut("123123", 0, 0));
    }


}
