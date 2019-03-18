package com.higgs.trust.rs.core.dao;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Date;
import java.util.List;

/**
 * CoreTransaction Dao test
 *
 * @author lingchao
 * @create 2018年08月24日17:26
 */
public class CoreTransactionDaoTest extends IntegrateBaseTest {
    @Autowired
    private CoreTransactionDao coreTransactionDao;

    @Test
    public void queryByTxIds() {
        List<String> txIdList = Lists.newArrayList("123");
        System.out.println(coreTransactionDao.queryByTxIds(txIdList));
    }

    @Test
    public void saveExecuteResultAndHeight() {
        System.out.println(coreTransactionDao.saveExecuteResultAndHeight("123", "OK", "KO", "chengggongn", 19999L));
    }

    @Test
    public void add() {

        CoreTransactionPO po = new CoreTransactionPO();
        po.setTxId("tx_id_" + "linchaoTest");
        po.setPolicyId("ppp-");
        po.setBizModel("{}");
        po.setSendTime(new Date());
        po.setLockTime(new Date());
        //   po.setExecuteResult("SUCCESS");
        //     po.setErrorMsg("aa");
        //   po.setErrorCode("000");
        po.setActionDatas("[]");
        po.setBlockHeight(1L);
        po.setCreateTime(new Date());
        po.setSender("sender-" + "linchaoTest");
        po.setVersion("v1.0.0");
        po.setSignDatas("[]");
        System.out.println(coreTransactionDao.add(po));
    }

}
