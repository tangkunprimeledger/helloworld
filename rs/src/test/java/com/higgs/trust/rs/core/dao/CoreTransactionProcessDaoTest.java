package com.higgs.trust.rs.core.dao;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * CoreTransactionProcessDao test
 *
 * @author lingchao
 * @create 2018年08月25日16:40
 */
public class CoreTransactionProcessDaoTest extends IntegrateBaseTest {
    @Autowired private CoreTransactionProcessDao coreTransactionProcessDao;
    @Test
    public void add(){
        CoreTransactionProcessPO coreTransactionProcessPO = new CoreTransactionProcessPO();
        coreTransactionProcessPO.setTxId("123");
        coreTransactionProcessPO.setStatus(CoreTxStatusEnum.INIT.getCode());
        System.out.println("add:"+coreTransactionProcessDao.add(coreTransactionProcessPO));
    }

    @Test
    public void queryByTxId(){
        System.out.println("queryByTxId with for update :"+coreTransactionProcessDao.queryByTxId("123", true));
        System.out.println("queryByTxId with not for update :"+coreTransactionProcessDao.queryByTxId("123", false));
    }


    @Test
    public void queryByStatus(){
        System.out.println("queryByStatus:"+coreTransactionProcessDao.queryByStatus(CoreTxStatusEnum.INIT.getCode(),0, 10));
    }


    @Test
    public void updateStatus(){
        System.out.println("updateStatus  to WAIT:"+coreTransactionProcessDao.updateStatus("123", CoreTxStatusEnum.INIT.getCode(),CoreTxStatusEnum.WAIT.getCode()));
        System.out.println("updateStatus  to END:"+coreTransactionProcessDao.updateStatus("123", CoreTxStatusEnum.WAIT.getCode(),CoreTxStatusEnum.END.getCode()));

    }

    @Test
    public void deleteEnd(){
        System.out.println("deleteEnd:"+coreTransactionProcessDao.deleteEnd());
    }



}
