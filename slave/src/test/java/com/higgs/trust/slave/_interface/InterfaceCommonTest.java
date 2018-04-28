package com.higgs.trust.slave._interface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.action.account.TestDataMaker;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuyu
 * @description
 * @date 2018-04-26
 */
public class InterfaceCommonTest extends BaseTest{
    @Autowired SnapshotService snapshotService;

    @BeforeMethod
    public void before(){
        snapshotService.startTransaction();
    }

    @AfterMethod
    public void after(){
        snapshotService.destroy();
    }

    public <T> T getBodyData(Map<?, ?> param,Class<T> clazz){
        String body = String.valueOf(param.get("body"));
        if(StringUtils.isEmpty(body) || "null".equals(body)){
            return null;
        }
        body = body.replaceAll(JSONObject.class.getCanonicalName(),clazz.getCanonicalName());
        return JSON.parseObject(body,clazz);
    }

    public String getAssertData(Map<?, ?> param){
        return String.valueOf(param.get("assert"));
    }

    public PackContext makePackContext(Action action,Long blockHeight) throws Exception {
        List<Action> actions = new ArrayList<>();
        actions.add(action);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 1, InitPolicyEnum.REGISTER);
        SignedTransaction transaction = TestDataMaker.makeSignedTx(coreTransaction);

        Package pack = new Package();
        pack.setHeight(blockHeight);

        List<SignedTransaction> signedTransactions = new ArrayList<>();
        signedTransactions.add(transaction);

        pack.setSignedTxList(signedTransactions);
        pack.setPackageTime(System.currentTimeMillis());
        pack.setStatus(PackageStatusEnum.INIT);

        BlockHeader header = new BlockHeader();
        header.setHeight(blockHeight);
        header.setBlockTime(System.currentTimeMillis());

        Block block = new Block();
        block.setBlockHeader(header);
        block.setSignedTxList(signedTransactions);

        PackContext packContext = new PackContext(pack, block);
        packContext.setCurrentAction(action);
        packContext.setCurrentTransaction(transaction);

        return packContext;
    }
}
