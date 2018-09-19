package com.higgs.trust.press;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author liuyu
 * @description
 * @date 2018-05-07
 */
public class AccountingService {
    /**
     * 操作的账户数，个数必须为2的N次幂
     */
    public final static int ACCOUNT_COUNT = 8;
    /**
     * 账号前缀
     */
    public final static String ACCOUNT_NO_PREFIX = "account_no_";
    /**
     * policy id
     */
    public final static String POLICY_ID = InitPolicyEnum.NA.getPolicyId();
    /**
     * the sender name
     */
    public static String SENDER = "TRUST-NODEB";

    /**
     * 冻结流水
     * key:账号
     * value:该账号对应的所有冻结流水号
     */
    private static Map<String, List<String>> FREEZE_FLOW_NO_MAP = new HashMap<>();

    public static void main(String[] args) throws Exception {
        List<Action> list = getOpenActions();
        setActionIndex(list);
        System.out.println(list);
        List<CoreTransaction> txs = getInOutTxs(true, true, new BigDecimal(100));
        System.out.println(JSON.toJSONString(txs));

        txs = getInOutTxs(false, false, new BigDecimal(100));
        System.out.println(JSON.toJSONString(txs));

        txs = getTransferTxs(true, new BigDecimal(10));

        System.out.println(JSON.toJSONString(txs));

        txs = getTransferTxs(false, new BigDecimal(10));

        System.out.println(JSON.toJSONString(txs));
    }

    /**
     * 获取 多个账户账号
     * 个数必须为2的N次幂
     *
     * @return
     */
    public static List<String> getAccountNos() {
        List<String> accountNos = new ArrayList<>();
        for (int i = 0; i < ACCOUNT_COUNT; i++) {
            accountNos.add(ACCOUNT_NO_PREFIX + i);
        }
        return accountNos;
    }

    /**
     * 获取借记账户号
     *
     * @return
     */
    public static List<String> getDebitAccountNos(List<String> accountNos) {
        List<String> accountNosOfDebit = new ArrayList<>();
        for (int i = 0; i < accountNos.size(); i++) {
            String accountNo = accountNos.get(i);
            if (i % 2 == 0) {
                accountNosOfDebit.add(accountNo);
            }
        }
        return accountNosOfDebit;
    }

    /**
     * 获取贷记账户号
     *
     * @return
     */
    public static List<String> getCreditAccountNos(List<String> accountNos) {
        List<String> accountNosOfCredit = new ArrayList<>();
        for (int i = 0; i < accountNos.size(); i++) {
            String accountNo = accountNos.get(i);
            if (i % 2 != 0) {
                accountNosOfCredit.add(accountNo);
            }
        }
        return accountNosOfCredit;
    }

    /**
     * 开户的 actions
     *
     * @return
     */
    public static List<Action> getOpenActions() {
        List<Action> list = new ArrayList<>();
        List<String> accountNos = getAccountNos();
        for (int i = 0; i < accountNos.size(); i++) {
            String accountNo = accountNos.get(i);
            list.add(TestDataMaker
                .makeOpenAccountAction(accountNo, i, i % 2 == 0 ? FundDirectionEnum.DEBIT : FundDirectionEnum.CREDIT));
        }
        return list;
    }

    /**
     * 创建开户的交易
     *
     * @return
     * @throws Exception
     */
    public static List<CoreTransaction> getOpenAccountTxs() throws Exception {
        List<CoreTransaction> txs = new ArrayList<>();
        List<Action> actions = getOpenActions();
        for(Action action : actions){
            List<Action> mActions = new ArrayList<>();
            mActions.add(action);
            setActionIndex(mActions);
            CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(mActions, 0, POLICY_ID, new JSONObject());
            txs.add(coreTransaction);
        }
        return txs;
    }

    /**
     * 获取出入金的 txs
     *
     * @param income true:入金,false:出金
     * @param moreTx true:一个交易一个action,返回多个交易
     *               false:单个交易多个action,返回一个交易
     * @param amount 交易额
     * @return
     * @throws Exception
     */
    public static List<CoreTransaction> getInOutTxs(boolean income, boolean moreTx, BigDecimal amount)
        throws Exception {
        List<String> accountNos = getAccountNos();
        List<String> accountNosOfDebit = getDebitAccountNos(accountNos);
        List<String> accountNosOfCredit = getCreditAccountNos(accountNos);
        List<Action> actions = new ArrayList<>();
        List<CoreTransaction> txs = new ArrayList<>();
        for (int i = 0; i < accountNos.size() / 2; i++) {
            Action action = TestDataMaker
                .makeOpertionAction(income ? accountNosOfDebit.get(i) : accountNosOfCredit.get(i),
                    income ? accountNosOfCredit.get(i) : accountNosOfDebit.get(i), amount);
            action.setIndex(i);
            if (moreTx) {
                actions = new ArrayList<>();
                actions.add(action);
                setActionIndex(actions);
                CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, i, POLICY_ID, new JSONObject());
                coreTransaction.setSender(SENDER);
                txs.add(coreTransaction);
            } else {
                actions.add(action);
            }
        }
        if (!moreTx) {
            setActionIndex(actions);
            CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, POLICY_ID, new JSONObject());
            txs.add(coreTransaction);
        }
        return txs;
    }

    /**
     * 获取转账的actions
     *
     * @param accountNos 方向一致的accountNo
     * @param amount     交易额
     * @return
     */
    private static List<Action> getTransferActions(List<String> accountNos, BigDecimal amount) {
        List<Action> actions = new ArrayList<>();
        int size = accountNos.size();

        for (int i = 0; i < size / 2; i++) {
            int index = new Random().nextInt(accountNos.size());
            String debitAccountNo = accountNos.get(index);
            String creditAccountNo = null;
            if (index == 0) {
                creditAccountNo = accountNos.get(index + 1);
            } else if (index == size - 1) {
                creditAccountNo = accountNos.get(index - 1);
            } else {
                if (index % 2 == 0) {
                    creditAccountNo = accountNos.get(index - 1);
                } else {
                    creditAccountNo = accountNos.get(index + 1);
                }
            }
            Action action = TestDataMaker.makeOpertionAction(debitAccountNo, creditAccountNo,
                amount == null ? new BigDecimal(1  + new Random().nextInt(500)) : amount);
            action.setIndex(i);
            actions.add(action);
        }

        return actions;
    }

    /**
     * 获取 转账txs
     *
     * @param moreTx true:一个交易一个action,返回多个交易
     *               false:单个交易多个action,返回一个交易
     * @param amount 交易额
     *               允许空值，空值时采用随机数
     * @return
     * @throws Exception
     */
    public static List<CoreTransaction> getTransferTxs(boolean moreTx, BigDecimal amount) throws Exception {
        List<String> accountNos = getAccountNos();

        List<String> accountNosOfDebit = getDebitAccountNos(accountNos);
        List<String> accountNosOfCredit = getCreditAccountNos(accountNos);

        List<Action> actions = getTransferActions(accountNosOfDebit, amount);
        actions.addAll(getTransferActions(accountNosOfCredit, amount));

        List<CoreTransaction> txs = new ArrayList<>();
        if (moreTx) {
            for (int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);
                List<Action> mActions = new ArrayList<>();
                mActions.add(action);
                setActionIndex(mActions);
                CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(mActions, i, POLICY_ID, new JSONObject());
                txs.add(coreTransaction);
            }
        } else {
            setActionIndex(actions);
            CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, POLICY_ID, new JSONObject());
            txs.add(coreTransaction);
        }
        return txs;
    }

    /**
     * 获取 冻结交易
     *
     * @param moreTx
     * @param amount 允许空值，空值时采用随机数
     * @return
     * @throws Exception
     */
    public static List<CoreTransaction> getFreezeTxs(boolean moreTx, BigDecimal amount) throws Exception {
        List<String> accountNos = getAccountNos();
        List<Action> actions = new ArrayList<>();

        List<CoreTransaction> txs = new ArrayList<>();

        int size = accountNos.size();
        for (int i = 0; i < size; i++) {
            String accountNo = accountNos.get(i);
            String bizFlow = "freeze_flow_no_" + System.currentTimeMillis() + new Random().nextInt(1000);
            Action action = TestDataMaker.makeFreezeAction(accountNo, bizFlow,
                amount == null ? new BigDecimal(1 + new Random().nextInt(10)) : amount);
            action.setIndex(i);
            if (moreTx) {
                List<Action> mActions = new ArrayList<>();
                mActions.add(action);
                setActionIndex(mActions);
                CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(mActions, i, POLICY_ID, new JSONObject());
                txs.add(coreTransaction);
            } else {
                actions.add(action);
            }
            //save flow no
            List<String> lastFreezeFlowNos = FREEZE_FLOW_NO_MAP.get(accountNo);
            if (CollectionUtils.isEmpty(lastFreezeFlowNos)) {
                lastFreezeFlowNos = new ArrayList<>();
            } else {
                if (lastFreezeFlowNos.size() > 500) {
                    lastFreezeFlowNos.clear();
                }
            }
            lastFreezeFlowNos.add(bizFlow);
            FREEZE_FLOW_NO_MAP.put(accountNo, lastFreezeFlowNos);
        }
        if (!moreTx) {
            setActionIndex(actions);
            JSONObject bizModel = new JSONObject();
            CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, POLICY_ID, bizModel);
            txs.add(coreTransaction);
        }
        return txs;
    }

    /**
     * 获取 解冻交易
     *
     * @param moreTx
     * @param amount 允许空值，空值时采用随机数
     * @return
     * @throws Exception
     */
    public static List<CoreTransaction> getUnFreezeTxs(boolean moreTx, BigDecimal amount) throws Exception {
        List<String> accountNos = getAccountNos();
        List<Action> actions = new ArrayList<>();

        List<CoreTransaction> txs = new ArrayList<>();

        int size = accountNos.size();
        for (int i = 0; i < size; i++) {
            String accountNo = accountNos.get(i);
            List<String> lastFreezeFlowNos = FREEZE_FLOW_NO_MAP.get(accountNo);
            String bizFlow = lastFreezeFlowNos.get(new Random().nextInt(lastFreezeFlowNos.size()));
            Action action = TestDataMaker.makeUnFreezeAction(accountNo, bizFlow,
                amount == null ? new BigDecimal(new Random().nextInt(20)) : amount);
            action.setIndex(i);
            if (moreTx) {
                List<Action> mActions = new ArrayList<>();
                mActions.add(action);
                setActionIndex(mActions);
                JSONObject bizModel = new JSONObject();
                CoreTransaction coreTx = TestDataMaker.makeCoreTx(mActions, i, POLICY_ID, bizModel);
                txs.add(coreTx);
            } else {
                actions.add(action);
            }
        }
        if (!moreTx) {
            setActionIndex(actions);
            JSONObject bizModel = new JSONObject();
            bizModel.put("data", actions);
            CoreTransaction coreTx = TestDataMaker.makeCoreTx(actions, 0, POLICY_ID, bizModel);
            txs.add(coreTx);
        }
        return txs;
    }

    /**
     * 设置 action 的 index
     * @param actions
     */
    private static void setActionIndex(List<Action> actions){
        if(CollectionUtils.isEmpty(actions)){
            return;
        }
        int index = 0;
        for(Action action : actions){
            action.setIndex(index);
            index++;
        }
    }
}
