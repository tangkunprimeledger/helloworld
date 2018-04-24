package com.higgs.trust.slave;

import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.transaction.TransactionExecutor;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/3/27 16:34
 */

public class MultiVersionTest extends BaseTest {

    @Autowired private TransactionExecutor executor;
    @Autowired private BlockService blockService;

    @Test public void packageRoot() {

        Package pack = new Package();
        pack.setHeight(1L);
        Block block = blockService.buildDummyBlock(1L, new Date().getTime());
        PackContext packContext = new PackContext(pack, block);

        SignedTransaction signedTransaction = new SignedTransaction();
        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setVersion(VersionEnum.V1.getCode());
        coreTransaction.setTxId(UUID.randomUUID().toString());

        RegisterRS accountingAction = new RegisterRS();
        accountingAction.setRsId("rs-wangxin");
        accountingAction.setType(ActionTypeEnum.REGISTER_RS);
        accountingAction.setIndex(12);
        List list = new LinkedList();
        list.add(accountingAction);

        coreTransaction.setActionList(list);

        signedTransaction.setCoreTx(coreTransaction);

        packContext.setCurrentTransaction(signedTransaction);

        executor.validate(packContext);

        coreTransaction.setVersion(VersionEnum.V1.getCode());
        signedTransaction.setCoreTx(coreTransaction);
        executor.validate(packContext);

        System.out.println("测试结束。。。。");
    }
}
