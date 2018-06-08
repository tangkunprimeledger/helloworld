package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.action.account.TestDataMaker;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-19
 */
public class PackageValidatorTest extends IntegrateBaseTest {

    @BeforeTest
    public void before(){

    }
    @Test
    public void testValidating() throws Exception {
        test(6L);
        test(7L);
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
//        for(int i=0;i<2;i++) {
//            List<Action> actions = new ArrayList<>();
//
//            accountNoOfDebit = "account_no_004_" + i;
//            accountNoOfCredit = "account_no_005_" + i;
//            Action debitAction = TestDataMaker.makeOpenAccountAction(accountNoOfDebit, FundDirectionEnum.DEBIT);
//            debitAction.setIndex(0);
//            actions.add(debitAction);
//
//            Action creditAction = TestDataMaker.makeOpenAccountAction(accountNoOfCredit, FundDirectionEnum.CREDIT);
//            creditAction.setIndex(1);
//            actions.add(creditAction);
//
//            Action operationAction = TestDataMaker.makeOpertionAction(accountNoOfDebit, accountNoOfCredit, new BigDecimal("8.80"));
//            operationAction.setIndex(2);
//            actions.add(operationAction);
//
//            AccountFreeze freezeAction = (AccountFreeze)TestDataMaker.makeFreezeAction(accountNoOfDebit,i);
//            freezeAction.setIndex(3);
//            actions.add(freezeAction);
//
//            freezeBizFlowNo = freezeAction.getBizFlowNo();
//
//            CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, i, InitPolicyEnum.REGISTER);
//            SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);
//            txs.add(tx);
//        }
        //
        List<Action> actions = new ArrayList<>();
        Action freezeAction = TestDataMaker.makeFreezeAction(accountNoOfCredit,3);
        freezeAction.setIndex(0);
        actions.add(freezeAction);

//        Action unfreezeAction = TestDataMaker.makeUnFreezeAction(accountNoOfDebit,freezeBizFlowNo);
//        unfreezeAction.setIndex(1);
//        actions.add(unfreezeAction);

        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 3, InitPolicyEnum.REGISTER_POLICY);
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);
        txs.add(tx);

        block.setSignedTxList(txs);
        pack.setSignedTxList(txs);

        PackContext packContext = new PackContext(pack,block);

//        packageValidator.validating(packContext);

//        packagePersistor.persisting(packContext);

    }
}
