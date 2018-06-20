package com.higgs.trust.slave.dao.account;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.account.AccountStateEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.dao.po.account.AccountInfoPO;
import com.higgs.trust.slave.model.bo.account.AccountFreezeRecord;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author liuyu
 * @description
 * @date 2018-06-20
 */
public class AccountJDBCDaoTest extends BaseTest {
    @Autowired AccountJDBCDao accountJDBCDao;
    @Autowired AccountInfoDao accountInfoDao;
    @Autowired TransactionTemplate txRequired;

    @Test public void testInsert() {
        List<AccountInfo> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setAccountNo("acc_ino_" + i);
            accountInfo.setCurrency("CNY");
            accountInfo.setBalance(BigDecimal.ZERO);
            accountInfo.setFreezeAmount(BigDecimal.ZERO);
            accountInfo.setFundDirection(FundDirectionEnum.CREDIT.getCode());
            accountInfo.setStatus(AccountStateEnum.NORMAL.getCode());
            accountInfo.setCreateTime(new Date());
            list.add(accountInfo);
        }
        int r = accountJDBCDao.batchInsertAccount(list);
        System.out.println(r);
    }

    @Test public void testUpdate() {
        List<AccountInfo> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setAccountNo("acc_ino_" + i);
            accountInfo.setCurrency("CNY");
            if(i % 2 == 0){
                accountInfo.setBalance(new BigDecimal(1));
                accountInfo.setFreezeAmount(new BigDecimal(new Random().nextInt(100)));
                System.out.println(i + "-" + accountInfo.getFreezeAmount());
            }else{
                accountInfo.setBalance(new BigDecimal(1000));
                accountInfo.setFreezeAmount(new BigDecimal(100));
            }
            accountInfo.setFundDirection(FundDirectionEnum.CREDIT.getCode());
            accountInfo.setStatus(AccountStateEnum.NORMAL.getCode());
            accountInfo.setCreateTime(new Date());
            list.add(accountInfo);
        }
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                int r = accountJDBCDao.batchUpdateAccount(list);
                System.out.println(r);
                r = 1/0;
            }
        });
    }

    @Test
    public void testBatchInsertCurrency() {
        List<CurrencyInfo> currencyInfos = new ArrayList<>();
        for(int i=0;i<10;i++){
            CurrencyInfo currencyInfo = new CurrencyInfo();
            currencyInfo.setCurrency("CNY_" + i);
            currencyInfo.setCreateTime(new Date());
            currencyInfos.add(currencyInfo);
        }
        accountJDBCDao.batchInsertCurrency(currencyInfos);
    }

    @Test public void testInsertFreeze() {
        List<AccountFreezeRecord> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            AccountFreezeRecord data = new AccountFreezeRecord();
            data.setAccountNo("acc_ino_" + i);
            data.setBizFlowNo("biz_flow_o_" + i);
            data.setAmount(BigDecimal.ZERO);
            data.setContractAddr("zz");
            data.setBlockHeight(1L);
            data.setCreateTime(new Date());
            list.add(data);
        }
        int r = accountJDBCDao.batchInsertFreezeRecord(list);
        System.out.println(r);
    }

    @Test public void testUpdateFreeze() {
        List<AccountFreezeRecord> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            AccountFreezeRecord data = new AccountFreezeRecord();
            data.setAccountNo("acc_ino_" + i);
            data.setBizFlowNo("biz_flow_o_" + i);
            data.setAmount(new BigDecimal(100));
            data.setContractAddr("xxxxxx" + i);
            data.setBlockHeight(1L);
            data.setCreateTime(new Date());
            list.add(data);
        }
        int r = accountJDBCDao.batchUpdateFreezeRecord(list);
        System.out.println(r);
    }

}
