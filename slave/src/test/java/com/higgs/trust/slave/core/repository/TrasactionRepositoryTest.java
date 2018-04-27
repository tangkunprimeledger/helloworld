package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-27
 */
public class TrasactionRepositoryTest extends IntegrateBaseTest {
    @Autowired TransactionRepository transactionRepository;

    @Test
    public void testQueryTxids(){
        List<SignedTransaction> txs = new ArrayList<>();

        for(int i=0;i<1;i++) {
            SignedTransaction signedTransaction = new SignedTransaction();
            CoreTransaction tx = new CoreTransaction();
            tx.setTxId("tx_id_3_1524628478627");
//            if(i == 1){
//                tx.setTxId("tx_id_1_1524200959951");
//            }
            tx.setActionList(new ArrayList<>());
            signedTransaction.setCoreTx(tx);
            txs.add(signedTransaction);
        }
//        String str = JSON.toJSONString(txs);
//        System.out.println(str);
//        transactionRepository.queryTxIds(txs);

        List<Action> actions = new ArrayList<>();
        String str = JSON.toJSONString(actions);
        System.out.println("str1-->" + str);
        actions = JSON.parseArray(str,Action.class);

        actions = null;
        str = JSON.toJSONString(actions);
        System.out.println("str2-->" + str);
        actions = JSON.parseArray(str,Action.class);

        System.out.println("str2-->" + str);
    }
}
