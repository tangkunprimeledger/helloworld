package com.higgs.trust.slave.api;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;

/**
 * Created by liuyu on 18/1/2.
 */
public class BlockchianServiceTest extends BaseTest {
    @Autowired
    private BlockChainService blockChainService;

    @Test
    public void testSave() {
    }

    @Test
    public void testRemove() {
    }

    @Test
    public void testQuerySystemPropertyByKey() {
        System.out.println("CHAIN_OWNER:" + blockChainService.querySystemPropertyByKey("CHAIN_OWNER"));
        System.out.println("UTXO_CONTRACT_ADDRESS:" + blockChainService.querySystemPropertyByKey("UTXO_CONTRACT_ADDRESS"));
    }


    @Test
    public void testQueryUTXOList() {
        List<TxIn> inputList = Lists.newArrayList();
        TxIn  txIn = new TxIn();
        txIn.setTxId("123123");
        txIn.setActionIndex(1);
        txIn.setIndex(0);
        inputList.add(txIn);

        System.out.println("queryUTXOList:" + blockChainService.queryUTXOList(inputList));

        System.out.println("getUTXOActionType:" + blockChainService.getUTXOActionType("NORMAL"));
    }
}
