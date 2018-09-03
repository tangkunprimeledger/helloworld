package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.account.*;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-03-28
 */
public class AccountingHandlerTest extends IntegrateBaseTest {
    @Autowired OpenAccountHandler openAccountHandler;
    @Autowired AccountOperationHandler accountOperationHandler;
    @Autowired AccountFreezeHandler accountFreezeHandler;
    @Autowired AccountUnFreezeHandler accountUnFreezeHandler;
    @Autowired BlockService blockService;
    @Autowired SnapshotService snapshotService;
    @Autowired IssueCurrencyHandler issueCurrencyHandler;

    @Before public void before() {
        snapshotService.startTransaction();

    }

    @After public void after() {

    }

    @Test public void testOpenAccount() {
        OpenAccount accountBO = new OpenAccount();
        accountBO.setType(ActionTypeEnum.OPEN_ACCOUNT);
        accountBO.setIndex(1);
        accountBO.setAccountNo("account_no_1002");
        accountBO.setChainOwner("BUC_CHAIN");
        accountBO.setDataOwner("rs-test2");
        accountBO.setCurrency("CNY");
        accountBO.setFundDirection(FundDirectionEnum.DEBIT);
        PackContext packContext = new PackContext(new Package(), new Block());
        packContext.setCurrentAction(accountBO);
     //   openAccountHandler.validate(packContext);
       // openAccountHandler.persist(packContext);
    }

    @Test public void testOperationAccount() throws Exception {
        AccountOperation action = new AccountOperation();
        action.setType(ActionTypeEnum.ACCOUNTING);
        action.setIndex(1);

        List<AccountTradeInfo> debitTradeInfo = new ArrayList<>();
        debitTradeInfo.add(new AccountTradeInfo("account_no_002", new BigDecimal("0.5")));
        List<AccountTradeInfo> creditTradeInfo = new ArrayList<>();
        creditTradeInfo.add(new AccountTradeInfo("account_no_003", new BigDecimal("0.5")));

        action.setBizFlowNo("biz_flow_no_" + System.currentTimeMillis());
        action.setDebitTradeInfo(debitTradeInfo);
        action.setCreditTradeInfo(creditTradeInfo);
        action.setAccountDate(new Date());

        PackContext packContext = new PackContext(new Package(), new Block());
        packContext.setCurrentAction(action);

        List<Action> actions = new ArrayList<>();
        actions.add(action);

        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 1, InitPolicyEnum.REGISTER_POLICY);
        SignedTransaction transaction = TestDataMaker.makeSignedTx(coreTransaction);

        packContext.setCurrentTransaction(transaction);
        Block block = new Block();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(1L);
        block.setBlockHeader(blockHeader);
        packContext.setCurrentBlock(block);

     //   accountOperationHandler.validate(packContext);
      //  accountOperationHandler.persist(packContext);
    }

    @Test public void testFreeze() throws Exception {
        AccountFreeze action = new AccountFreeze();
        action.setType(ActionTypeEnum.FREEZE);
        action.setIndex(1);
        action.setBizFlowNo("freeze_flow_no_001_" + System.currentTimeMillis());
        action.setAccountNo("account_no_005_1");
        action.setAmount(new BigDecimal("0.01"));
        action.setContractAddr(null);

        List<Action> actions = new ArrayList<>();
        actions.add(action);

        SignedTransaction transaction =
            TestDataMaker.makeSignedTx(TestDataMaker.makeCoreTx(actions, 1, InitPolicyEnum.REGISTER_POLICY));
        Package pack = new Package();
        pack.setHeight(1L);
        List<SignedTransaction> signedTransactions = new ArrayList<>();
        signedTransactions.add(transaction);
        pack.setSignedTxList(signedTransactions);
        pack.setPackageTime(System.currentTimeMillis());
        pack.setStatus(PackageStatusEnum.RECEIVED);

        Block block = blockService.buildDummyBlock(1L, new Date().getTime());
        block.setSignedTxList(signedTransactions);
        PackContext packContext = new PackContext(pack, block);

        packContext.setCurrentAction(action);

        packContext.setCurrentTransaction(transaction);

    //    accountFreezeHandler.validate(packContext);
      //  accountFreezeHandler.persist(packContext);
    }

    @Test public void testUnFreeze() throws Exception {
        Package pack = new Package();
        pack.setHeight(1L);
        Block block = blockService.buildDummyBlock(1L, new Date().getTime());
        PackContext packContext = new PackContext(pack, block);

        AccountUnFreeze action = new AccountUnFreeze();
        action.setType(ActionTypeEnum.UNFREEZE);
        action.setIndex(1);
        action.setBizFlowNo("freeze_flow_no_001_1524025059765");
        action.setAccountNo("account_no_005_1");
        action.setAmount(new BigDecimal("0.01"));

        packContext.setCurrentAction(action);

        List<Action> actions = new ArrayList<>();
        actions.add(action);

        SignedTransaction transaction =
            TestDataMaker.makeSignedTx(TestDataMaker.makeCoreTx(actions, 1, InitPolicyEnum.REGISTER_POLICY));

        packContext.setCurrentTransaction(transaction);

    //    accountUnFreezeHandler.validate(packContext);
      //  accountUnFreezeHandler.persist(packContext);
    }

    @Test public void testIssueCurrency() {
        Action action = TestDataMaker.makeCurrencyAction("CNY");
        PackContext packContext = new PackContext(new Package(), new Block());
        packContext.setCurrentAction(action);
      //  issueCurrencyHandler.validate(packContext);
      //  issueCurrencyHandler.persist(packContext);
    }
}
