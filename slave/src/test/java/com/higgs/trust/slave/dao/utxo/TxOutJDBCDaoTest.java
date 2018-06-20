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
public class TxOutJDBCDaoTest extends BaseTest {

    @Autowired private TxOutJDBCDao txOutDao;

    public void batchInsertTest() {
        long count = 0L;
        do {
            List<TxOutPO> txOutPOList = new ArrayList<>();
            for(int i=0;i<1;i++){
                TxOutPO txOutPO = new TxOutPO();
                txOutPO.setTxId("123123345234");
                txOutPO.setStatus(UTXOStatusEnum.UNSPENT.getCode());
                txOutPO.setActionIndex(0);
                txOutPO.setIndex(i);
                txOutPO.setContractAddress("qqqqq");
                txOutPO.setState("{\"@type\":\"com.alibaba.fastjson.JSONObject\",\"amount\":1}");
                txOutPO.setStateClass("afdaf");
                txOutPO.setIdentity("12312312");
                txOutPOList.add(txOutPO);
            }
            TxOutPO txOutPO = new TxOutPO();
            txOutPO.setTxId("12312322223412312");
            txOutPO.setStatus(UTXOStatusEnum.UNSPENT.getCode());
            txOutPO.setActionIndex(0);
            txOutPO.setIndex(0);
            txOutPO.setContractAddress("qqqqq");
            txOutPO.setState("{\"@type\":\"com.alibaba.fastjson.JSONObject\",\"amount\":1}");
            txOutPO.setStateClass("afdaf");
            txOutPO.setIdentity("12312312");
            txOutPOList.add(txOutPO);
            count++;
            System.out.println("count ："+ count +"  batchInsert :" + txOutDao.batchInsert(txOutPOList)+"  size= "+txOutPOList.size());
        }while (count!=1L);
    }

     public void batchUpdateTest() {

        List<TxOutPO> txOutPOList = new ArrayList<>();
        TxOutPO txOutPO = new TxOutPO();
        txOutPO.setTxId("123123345234");
        txOutPO.setSTxId("123123" + System.currentTimeMillis());
        txOutPO.setStatus(UTXOStatusEnum.SPENT.getCode());
        txOutPO.setActionIndex(0);
        txOutPO.setIndex(0);

        TxOutPO txOutPO1 = new TxOutPO();
        txOutPO1.setTxId("12312322223412312");
        txOutPO1.setSTxId("123123" + System.currentTimeMillis());
        txOutPO1.setStatus(UTXOStatusEnum.SPENT.getCode());
        txOutPO1.setActionIndex(0);
        txOutPO1.setIndex(0);


        txOutPOList.add(txOutPO);
        txOutPOList.add(txOutPO1);

        System.out.println("batchUpdate :" + txOutDao.batchUpdate(txOutPOList));
    }

    @Test public void batchAllTest() {
        batchInsertTest();
        batchUpdateTest();
    }

}
