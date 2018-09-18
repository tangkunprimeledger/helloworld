package com.higgs.trust.press;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.presstest.vo.*;
import com.higgs.trust.slave.model.bo.account.AccountTradeInfo;
import org.testng.collections.Lists;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author liuyu
 * @description
 * @date 2018-09-03
 */
public class AccountTest extends BasePressTest{

    public static void main(String[] args) throws IOException {
//        init();
          test();
    }

    /**
     * 初始化
     */
    private static void init() {
        AccountTest accountTest = new AccountTest();
//                accountTest.createCurrency();
//                accountTest.openAccount();
                accountTest.income();
        //        accountTest.out();
        //               accountTest.transfer();
//        accountTest.freeze();
    }

    /**
     * 测试
     *
     * @throws IOException
     */
    private static void test() throws IOException {
        for (int i = 0; i < 1000; i++) {
            new Thread(new MyTask()).start();
        }
        //wait
        System.in.read();
    }

    /**
     * task
     */
    static class MyTask implements Runnable {
        private AccountTest accountTest = new AccountTest();
        @Override public void run() {
            while (true) {
                exe();
            }
        }
        //
        private int exe() {
            int num = 0;
            num += accountTest.transfer();
            num += accountTest.income();
            num += accountTest.out();
            num += accountTest.freeze();
            num += accountTest.unfreeze();
            return num;
        }
    }

    //冻结记录
    private Map<String, String> freezeMap = new HashMap<>();

    /**
     * 创建币种
     */
    public void createCurrency() {
        CurrencyVO vo = new CurrencyVO();
        vo.setReqNo("tx_id_create_currency_" + System.currentTimeMillis() + new Random().nextInt(1000) + "-" + Thread.currentThread().getName());
        vo.setCurrencyName("CNY");
        vo.setRemark("币种");
        send("press/createCurrency", JSON.toJSONString(vo));
    }

    /**
     * 开户
     */
    public void openAccount() {
        for (int i = 0; i < 8; i++) {
            OpenAccountVO vo = new OpenAccountVO();
            vo.setReqNo("tx_id_open_account_" + i + "_" + System.currentTimeMillis() + new Random().nextInt(1000) + "-" + Thread.currentThread().getName());
            vo.setCurrencyName("CNY");
            vo.setAccountNo("account_no_" + i);
            if (i % 2 == 0) {
                vo.setFundDirection(0);
            } else {
                vo.setFundDirection(1);
            }
            send("press/openAccount", JSON.toJSONString(vo));
            sleep();
        }
    }

    /**
     * 入金
     */
    public int income() {
        int num = 0;
        for (int i = 0; i < 7; i = i + 2) {
            BigDecimal amount = new BigDecimal(5000);
            AccountingVO vo = new AccountingVO();
            vo.setReqNo("tfs_tx_id_income_" + i + "_" + System.currentTimeMillis() + new Random().nextInt(1000) + "-" + Thread.currentThread().getName());
            vo.setDebitTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_" + i, amount)));
            vo.setCreditTradeInfo(
                Lists.newArrayList(new AccountTradeInfo("account_no_" + (i + 1), amount)));
            send("press/accounting", JSON.toJSONString(vo));
            sleep();
            num++;
        }
        return num;
    }

    /**
     * 出金
     */
    public int out() {
        int num = 0;
        for (int i = 0; i < 7; i = i + 2) {
            AccountingVO vo = new AccountingVO();
            vo.setReqNo("tfs_tx_id_out_" + i + "_" + System.currentTimeMillis() + new Random().nextInt(1000) + "-" + Thread.currentThread().getName());
            BigDecimal amount = new BigDecimal(10);
            vo.setDebitTradeInfo(
                Lists.newArrayList(new AccountTradeInfo("account_no_" + (i + 1), amount)));
            vo.setCreditTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_" + i,amount)));
            send("press/accounting", JSON.toJSONString(vo));
            sleep();
            num++;
        }
        return num;
    }

    /**
     * 转账
     */
    public int transfer() {
        int num = 0;
        for (int i = 0; i < 6; i++) {
            BigDecimal amount = new BigDecimal(new Random().nextInt(100) + 1);
            AccountingVO vo = new AccountingVO();
            vo.setReqNo("tfs_tx_id_transfer_" + i + "_" + System.currentTimeMillis() + new Random().nextInt(1000) + "-" + Thread.currentThread().getName());
            if (i % 2 == 0) {
                vo.setDebitTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_" + i, amount)));
                vo.setCreditTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_" + (i + 2), amount)));
            } else {
                vo.setDebitTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_" + i, amount)));
                vo.setCreditTradeInfo(Lists.newArrayList(new AccountTradeInfo("account_no_" + (i + 2), amount)));
            }
            send("press/accounting", JSON.toJSONString(vo));
            sleep();
            num++;
        }
        return num;
    }

    /**
     * 冻结
     */
    public int freeze() {
        int num = 0;
        for (int i = 0; i < 8; i++) {
            FreezeVO vo = new FreezeVO();
            vo.setReqNo("tfs_tx_id_freeze_" + i + "_" + System.currentTimeMillis() + new Random().nextInt(1000) + "-" + Thread.currentThread().getName());
            vo.setAccountNo("account_no_" + i);
            vo.setBizFlowNo("biz_flow_no_" + i + "_" + System.currentTimeMillis() + new Random().nextInt(1000));
            BigDecimal amount = new BigDecimal(new Random().nextInt(10) + 1);
            vo.setAmount(amount);
            freezeMap.put(vo.getAccountNo(), vo.getBizFlowNo());
            send("press/freeze", JSON.toJSONString(vo));
            sleep();
            num++;
        }
        return num;
    }

    /**
     * 解冻
     */
    public int unfreeze() {
        int num = 0;
        for (int i = 0; i < 8; i++) {
            UnFreezeVO vo = new UnFreezeVO();
            vo.setReqNo("tfs_tx_id_unfreeze_" + i + "_" + System.currentTimeMillis() + new Random().nextInt(1000) + "-" + Thread.currentThread().getName());
            vo.setAccountNo("account_no_" + i);
            vo.setBizFlowNo(freezeMap.remove(vo.getAccountNo()));
            BigDecimal amount = new BigDecimal("0.1");
            vo.setAmount(amount);
            send("press/unfreeze", JSON.toJSONString(vo));
            sleep();
            num++;
        }
        return num;
    }


    private void sleep() {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//        }
    }
}
