package com.higgs.trust.press;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author liuyu
 * @description
 * @date 2018-09-19
 */
public class BatchTest extends BasePressTest{

    public static void main(String[] args) throws Exception {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //JSON不做循环引用检测
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        //JSON输出NULL属性
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        //toJSONString的时候对一级key进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.SortField.getMask();
        //toJSONString的时候对嵌套结果进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.MapSortField.getMask();
        //toJSONString的时候记录Class的name
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteClassName.getMask();

//       new BatchTest().exe();
        test();
    }

    /**
     * 测试
     *
     * @throws IOException
     */
    private static void test() throws IOException {
        for (int i = 0; i < 10; i++) {
            new Thread(new BatchTest.MyTask()).start();
        }
        //wait
        System.in.read();
    }

    static class MyTask implements Runnable{
        @Override public void run() {
            while (true) {
                try {
                    new BatchTest().exe();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void exe() throws Exception {
        AccountingService.SENDER = "TRUST-NODEB";
        List<CoreTransaction> txs = BatchTest.getOpenAccountTxs();
        txs.addAll(BatchTest.getInComeTxs());
        txs.addAll(BatchTest.getOutTxs());
        txs.addAll(BatchTest.getTransferTxs());
        txs.addAll(BatchTest.getFreezeTxs());
        txs.addAll(BatchTest.getUnFreezeTxs());
        send("press/batch", JSON.toJSONString(txs));
    }

    //构建创建账户的交易
    public static List<CoreTransaction> getOpenAccountTxs(){
        List<CoreTransaction> txs = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String accountNo = "batch_account_no_" + System.currentTimeMillis() + "_" + new Random().nextInt(10000);
            List<Action> actions = new ArrayList<>();
            actions.add(TestDataMaker
                .makeOpenAccountAction(accountNo, 0, i % 2 == 0 ? FundDirectionEnum.DEBIT : FundDirectionEnum.CREDIT));
            CoreTransaction coreTransaction =
                TestDataMaker.makeCoreTx(actions, 0, AccountingService.POLICY_ID, new JSONObject());
            txs.add(coreTransaction);
        }
        return txs;
    }

    //构建账户入金的交易
    public static List<CoreTransaction> getInComeTxs() throws Exception {
        List<CoreTransaction> txs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            txs.addAll(AccountingService.getInOutTxs(true, true, new BigDecimal(1000)));
        }
        return txs;
    }

    //构建账户出金的交易
    public static List<CoreTransaction> getOutTxs() throws Exception {
        List<CoreTransaction> txs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            txs.addAll(AccountingService.getInOutTxs(false, true, new BigDecimal(1)));
        }
        return txs;
    }

    //构建账户互转的交易
    public static List<CoreTransaction> getTransferTxs() throws Exception {
        List<CoreTransaction> txs = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            txs.addAll(AccountingService.getTransferTxs(true, null));
        }
        return txs;
    }

    //构建账户冻结的交易
    public static List<CoreTransaction> getFreezeTxs() throws Exception {
        List<CoreTransaction> txs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            txs.addAll(AccountingService.getFreezeTxs(true, null));
        }
        return txs;
    }

    //构建账户解冻的交易
    public static List<CoreTransaction> getUnFreezeTxs() throws Exception {
        List<CoreTransaction> txs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            txs.addAll(AccountingService.getUnFreezeTxs(true, new BigDecimal(0.001)));
        }
        return txs;
    }

}
