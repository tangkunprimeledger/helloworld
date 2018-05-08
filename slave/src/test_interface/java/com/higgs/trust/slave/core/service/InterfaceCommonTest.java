package com.higgs.trust.slave.core.service;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.JsonFileUtil;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.action.account.TestDataMaker;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;


/**
 *
 * @author liuyu
 * @description
 * @date 2018-04-26
 */
@Slf4j public abstract class InterfaceCommonTest extends BaseTest {
    /**
     * 数据库连接定义
     */
    public static String DB_URL = "jdbc:mysql://localhost:3306/trust?user=root&password=root";
    /**
     * snapshot service
     */
    @Autowired SnapshotService snapshotService;

    @BeforeMethod public void before() {
        snapshotService.startTransaction();
    }

    @AfterMethod public void after() {
        snapshotService.destroy();
    }

    /**
     * 测试数据根路径
     *
     * @return
     */
    protected abstract String getProviderRootPath();

    /**
     * 默认的测试数据源，路径：测试数据根路径+测试方法名
     *
     * @param method
     * @return
     */
    @DataProvider public Object[][] defaultProvider(Method method) {
        String name = method.getName();
        String providerPath = getProviderRootPath() + name;
        log.info("[defaultProvider].path:{}", providerPath);
        String filePath = JsonFileUtil.findJsonFile(providerPath);
        HashMap<String, String>[][] arrMap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filePath);
        return arrMap;
    }

    /**
     * 执行actionHandler, validate、persist都会执行
     *
     * @param param
     * @param actionHandler
     * @param action
     */
    protected void executeActionHandler(Map<?, ?> param,ActionHandler actionHandler,Action action){
        String assertData = getAssertData(param);
        try {
            actionHandler.validate(makePackContext(action, 1L));
            actionHandler.persist(makePackContext(action, 1L));
        }catch (Exception e){
            log.info("has error:{}",e.getMessage());
            assertEquals(e.getMessage(),assertData);
        }
    }

    /**
     * 获取 测试数据中的 body 对象实体
     * @param param
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> T getBodyData(Map<?, ?> param, Class<T> clazz) {
        String body = String.valueOf(param.get("body"));
        if (StringUtils.isEmpty(body) || "null".equals(body)) {
            return null;
        }
        body = body.replaceAll("\"@type\":\"com.alibaba.fastjson.JSONObject\",","");
        return JSON.parseObject(body, clazz);
    }

    /**
     * 从body中获取action对象实体,同时设置actionType

     * @param param
     * @param actionTypeEnum
     * @return
     */
    protected <T> T getAction(Map<?, ?> param, Class<T> clazz,ActionTypeEnum actionTypeEnum) {
        T data = getBodyData(param,clazz);
        if(data == null){
            return null;
        }
        Action action = (Action)data;
        action.setType(actionTypeEnum);
        action.setIndex(1);
        return (T)action;
    }

    /**
     * 断言
     *
     * @param param
     * @return
     */
    protected String getAssertData(Map<?, ?> param) {
        return String.valueOf(param.get("assert"));
    }

    /**
     * package data 的 封装
     *
     * @param action
     * @param blockHeight
     * @return
     * @throws Exception
     */
    protected PackContext makePackContext(Action action, Long blockHeight) throws Exception {
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
