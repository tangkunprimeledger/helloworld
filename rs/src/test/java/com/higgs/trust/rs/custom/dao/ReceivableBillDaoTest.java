package com.higgs.trust.rs.custom.dao;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.custom.api.enums.BillStatusEnum;
import com.higgs.trust.rs.custom.dao.po.ReceivableBillPO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ReceivableBillDao test
 *
 * @author lingchao
 * @create 2018年05月14日15:24
 */
public class ReceivableBillDaoTest extends IntegrateBaseTest {
    @Autowired
    private ReceivableBillDao receivableBillDao;

    @Test
    public void addTest() {
        ReceivableBillPO receivableBillPO = new ReceivableBillPO();
        receivableBillPO.setState("ssd");
        receivableBillPO.setContractAddress("12312");
        receivableBillPO.setIndex(0L);
        receivableBillPO.setActionIndex(0L);
        receivableBillPO.setTxId("123123");
        receivableBillPO.setStatus(BillStatusEnum.PROCESS.getCode());
        receivableBillPO.setHolder("lingchao");
        receivableBillPO.setBillId("12345");

        System.out.println("--------------------------------------------" + receivableBillDao.add(receivableBillPO));

    }

    @Test
    public void updateTest() {
        System.out.println("--------------------------------------------" + receivableBillDao.updateStatus("123123", 0L,
        0L,BillStatusEnum.PROCESS.getCode(), BillStatusEnum.UNSPENT.getCode()));
    }


    @Test
    public void queryByListTest() {
        ReceivableBillPO receivableBillParam = new ReceivableBillPO();
        receivableBillParam.setBillId("12345");
        receivableBillParam.setStatus(BillStatusEnum.UNSPENT.getCode());

        System.out.println("--------------------------------------------" + receivableBillDao.queryByList(receivableBillParam));

    }

}
