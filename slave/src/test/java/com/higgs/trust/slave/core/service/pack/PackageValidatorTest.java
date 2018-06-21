package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.action.account.TestDataMaker;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.account.AccountFreeze;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-19
 */
public class PackageValidatorTest extends IntegrateBaseTest {
    @Autowired PackageService packageService;
    @Autowired private TransactionTemplate txRequired;

    @BeforeTest
    public void before(){

    }
    @Test
    public void testValidating() throws Exception {
        test(2L);
//        test2(3L);
    }

    private void test(Long height) throws Exception {
        Block block = new Block();
        Package pack = new Package();
        pack.setHeight(height);
        pack.setPackageTime(System.currentTimeMillis());
        BlockHeader blockHeader = TestDataMaker.makeBlockHeader();
        block.setBlockHeader(blockHeader);

        List<SignedTransaction> txs = new ArrayList<>();

        String accountNoOfDebit = null;
        String accountNoOfCredit = "account_no_005_1";
        String freezeBizFlowNo = null;
        for(int i=0;i<1;i++) {
            List<Action> actions = new ArrayList<>();

            accountNoOfDebit = "account_no_001_" + i;
            accountNoOfCredit = "account_no_002_" + i;
            Action debitAction = TestDataMaker.makeOpenAccountAction(accountNoOfDebit, FundDirectionEnum.DEBIT);
            debitAction.setIndex(0);
            actions.add(debitAction);

            Action creditAction = TestDataMaker.makeOpenAccountAction(accountNoOfCredit, FundDirectionEnum.CREDIT);
            creditAction.setIndex(1);
            actions.add(creditAction);

            Action operationAction = TestDataMaker.makeOpertionAction(accountNoOfDebit, accountNoOfCredit, new BigDecimal("10"));
            operationAction.setIndex(2);
            actions.add(operationAction);
//
            AccountFreeze freezeAction = (AccountFreeze)TestDataMaker.makeFreezeAction(accountNoOfDebit,i);
            freezeAction.setIndex(3);
            actions.add(freezeAction);

            freezeBizFlowNo = freezeAction.getBizFlowNo();

            CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, i, InitPolicyEnum.REGISTER_POLICY);
            SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);
            txs.add(tx);
        }
        //
        List<Action> actions = new ArrayList<>();
//        Action freezeAction = TestDataMaker.makeFreezeAction(accountNoOfCredit,3);
//        freezeAction.setIndex(0);
//        actions.add(freezeAction);

        Action unfreezeAction = TestDataMaker.makeUnFreezeAction(accountNoOfDebit,freezeBizFlowNo);
        unfreezeAction.setIndex(1);
        actions.add(unfreezeAction);

        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 3, InitPolicyEnum.REGISTER_POLICY);
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);
        txs.add(tx);

        block.setSignedTxList(txs);
        pack.setSignedTxList(txs);

        PackContext packContext = new PackContext(pack,block);
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                packageService.process(packContext,false,false);
            }
        });
    }

    private void test2(Long height) throws Exception {
        Block block = new Block();
        Package pack = new Package();
        pack.setHeight(height);
        pack.setPackageTime(System.currentTimeMillis());
        BlockHeader blockHeader = TestDataMaker.makeBlockHeader();
        block.setBlockHeader(blockHeader);

        List<SignedTransaction> txs = new ArrayList<>();

        String accountNoOfDebit = null;
        String accountNoOfCredit = "account_no_005_1";
        String freezeBizFlowNo = null;
        for(int i=0;i<1;i++) {
            List<Action> actions = new ArrayList<>();

            accountNoOfDebit = "account_no_001_" + i;
            accountNoOfCredit = "account_no_002_" + i;

            Action operationAction = TestDataMaker.makeOpertionAction(accountNoOfDebit, accountNoOfCredit, new BigDecimal("5"));
            operationAction.setIndex(2);
            actions.add(operationAction);
            //
            AccountFreeze freezeAction = (AccountFreeze)TestDataMaker.makeFreezeAction(accountNoOfDebit,i);
            freezeAction.setIndex(3);
            actions.add(freezeAction);

            freezeBizFlowNo = freezeAction.getBizFlowNo();

            CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, i, InitPolicyEnum.REGISTER_POLICY);
            SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);
            txs.add(tx);
        }
        //
        List<Action> actions = new ArrayList<>();
        //        Action freezeAction = TestDataMaker.makeFreezeAction(accountNoOfCredit,3);
        //        freezeAction.setIndex(0);
        //        actions.add(freezeAction);

        Action unfreezeAction = TestDataMaker.makeUnFreezeAction(accountNoOfDebit,freezeBizFlowNo);
        unfreezeAction.setIndex(1);
        actions.add(unfreezeAction);

        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 3, InitPolicyEnum.REGISTER_POLICY);
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);
        txs.add(tx);

        block.setSignedTxList(txs);
        pack.setSignedTxList(txs);

        PackContext packContext = new PackContext(pack,block);
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                packageService.process(packContext,false,false);
            }
        });
    }
}
