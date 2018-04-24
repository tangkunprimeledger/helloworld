package com.higgs.trust.slave.dao.transaction;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.dao.po.transaction.ActionPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.*;

public class ActionDaoTest extends BaseTest {
    @Autowired
    private ActionDao actionDao;
    @Test
    public void testBatchInsert() throws Exception {
        List<ActionPO> list = new ArrayList<>();
        for(int i= 0; i<10;i++){
            ActionPO  actionPO  = new ActionPO();
            actionPO.setCreateTime(new Date());
            actionPO.setIndex(i);
            actionPO.setTxId(System.currentTimeMillis()+"");
            actionPO.setType(ActionTypeEnum.UTXO.getCode());
            actionPO.setData("werwe");
            list.add(actionPO);
        }
        System.out.println( actionDao.batchInsert(list));
    }

}