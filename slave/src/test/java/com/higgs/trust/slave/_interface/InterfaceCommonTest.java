package com.higgs.trust.slave._interface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
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
import com.higgs.trust.tester.assertutil.AssertTool;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;

import static org.testng.Assert.assertEquals;

/**
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
    private Map<?, ?> param = null;
    public void setParam(Map<?, ?> param){
        this.param = param;
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
    protected void executeActionHandler(Map<?, ?> param, ActionHandler actionHandler, Action action) {
        String assertData = getAssertData(param);
        try {
            PackContext packContext = makePackContext(action, 1L,param);
            String policyId = getPolicyId(param);
            if(!StringUtils.isEmpty(policyId) && !StringUtils.equals("null",policyId)){
                packContext.getCurrentTransaction().getCoreTx().setPolicyId(policyId);
            }
            actionHandler.validate(packContext);
            actionHandler.persist(packContext);
        } catch (Exception e) {
            log.info("has error:{}", e.getMessage());
            assertEquals(e.getMessage(), assertData);
        }
    }
    /**
     * 执行 前置sql
     *
     * @param param
     */
    protected void executeBeforeSql(Map<?, ?> param) {
        executeSql(getBeforeSql(param));
    }

    /**
     * 执行 查询sql
     *
     * @param param
     * @return
     */
    protected List<JSONArray> executeQuerySql(Map<?, ?> param) {
        return executeSql(getQuerySql(param));
    }

    /**
     * 执行 后置sql
     *
     * @param param
     */
    protected void executeAfterSql(Map<?, ?> param) {
        executeSql(getAfterSql(param));
    }
    /**
     * 获取 测试数据中的 body 对象实体
     *
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
        body = body.replaceAll("\"@type\":\"com.alibaba.fastjson.JSONObject\",", "");
        return JSON.parseObject(body, clazz);
    }

    /**
     * 根据传入的json反序列化为对象
     * @param content
     * @param clazz
     * @param <T>
     * @return
     */
    protected  <T> T getObject(String content,Class<T> clazz){
        if(StringUtils.isEmpty(content)){
            return null;
        }

        content= content.replaceAll("\"@type\":\"com.alibaba.fastjson.JSONObject\",","");
        return JSON.parseObject(content, clazz);
    }

    /**
     * 从body中获取action对象实体,同时设置actionType
     *
     * @param param
     * @param actionTypeEnum
     * @return
     */
    protected <T> T getAction(Map<?, ?> param, Class<T> clazz, ActionTypeEnum actionTypeEnum) {
        T data = getBodyData(param, clazz);
        if (data == null) {
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
     * 获取 前置sql
     *
     * @param param
     * @return
     */
    protected List<String> getBeforeSql(Map<?, ?> param) {
        String sql = String.valueOf(param.get("beforeSql"));
        return parseSqls(sql);
    }

    /**
     * 获取 查询sql
     *
     * @param param
     * @return
     */
    protected List<String> getQuerySql(Map<?, ?> param) {
        String sql = String.valueOf(param.get("querySql"));
        return parseSqls(sql);
    }

    /**
     * 获取 后置sql
     *
     * @param param
     * @return
     */
    protected List<String> getAfterSql(Map<?, ?> param) {
        String sql = String.valueOf(param.get("afterSql"));
        return parseSqls(sql);
    }

    /**
     * 验证操作结果
     * 1.执行查询sql-->用例数据key:querySql
     * 2.遍历查询结果
     * 3.比对每一个结果集中字段值是否跟预期的结果值一致-->用例数据key:assertData
     * 4.用例中预期数据支持多个，需确保顺序与查询结果集一致
     * @param param
     */
    protected void checkResults(Map<?, ?> param){
        List<JSONArray> queryResult = executeQuerySql(param);
        if(CollectionUtils.isEmpty(queryResult)){
            throw new RuntimeException("query data is empty");
        }
        int index = 0;
        for(JSONArray jsonArray : queryResult){
            int size = jsonArray.size();
            if(size == 0){
                throw new RuntimeException("query data.item is empty");
            }
            JSONObject assertData = getAssertDataByIndex(param,index);
            if(assertData == null){
                throw new RuntimeException("get assertData is error");
            }
            index++;
            for(int i=0;i<size;i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                AssertTool.isContainsExpect(assertData,jsonObject);
            }
        }
    }
    /**
     * 获取 断言数据
     *
     * @param param
     * @param index
     *
     * @return
     */
    protected JSONObject getAssertDataByIndex(Map<?, ?> param,int index){
        List<JSONObject> list = getAssertDatas(param);
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        return list.get(index);
    }

    /**
     * 获取 断言数据
     *
     * @param param
     * @return
     */
    protected List<JSONObject> getAssertDatas(Map<?, ?> param){
        String sql = String.valueOf(param.get("assertData"));
        return JSON.parseArray(sql, JSONObject.class);
    }


    /**
     * 解析sql json
     *
     * @param sql
     * @return
     */
    private List<String> parseSqls(String sql) {
        List<String> sqlArr = null;
        try {
            sqlArr = JSON.parseArray(sql, String.class);
        } catch (Exception e) {
            log.error("parse arr has error", e);
        }
        if (!CollectionUtils.isEmpty(sqlArr)) {
            return sqlArr;
        }
        sqlArr = Arrays.asList(sql.split(";"));
        return sqlArr;
    }

    /**
     * 执行sql
     *
     * @param sqls
     */
    protected List<JSONArray> executeSql(List<String> sqls) {
        if (CollectionUtils.isEmpty(sqls)) {
            return null;
        }
        List<JSONArray> queryResult = new ArrayList<>();
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection conn = dataBaseManager.getMysqlConnection(DB_URL);
        log.info("start execute sqls:{}",sqls);
        for (String sql : sqls) {
            log.info("execute.sql:{}",sql);
            if (sql.toUpperCase().contains("INSERT")) {
                dataBaseManager.executeInsert(sql, conn);
            } else if (sql.toUpperCase().contains("UPDATE")) {
                dataBaseManager.executeUpdate(sql, conn);
            } else if (sql.toUpperCase().contains("DELETE") || sql.toUpperCase().contains("TRUNCATE")) {
                dataBaseManager.executeDelete(sql, conn);
            } else if (sql.toUpperCase().contains("SELECT")) {
                JSONArray jsonArray = dataBaseManager.executeQuery(sql,conn);
                queryResult.add(jsonArray);
            }
        }
        dataBaseManager.closeConnection(conn);
        log.info("end execute sqls:{}",sqls);
        return queryResult;
    }

    /**
     * 从case中获取policyId
     *
     * @param param
     * @return
     */
    protected String getPolicyId(Map<?,?> param){
        String pid = String.valueOf(param.get("policyId"));
        return StringUtils.isEmpty(pid)?InitPolicyEnum.REGISTER.getPolicyId():pid;
    }
    /**
     * package data 的 封装
     *
     * @param action
     * @param blockHeight
     * @return
     * @throws Exception
     */
    protected PackContext makePackContext(Action action, Long blockHeight,Map<?, ?> param) throws Exception {
        List<Action> actions = new ArrayList<>();
        actions.add(action);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 1, InitPolicyEnum.getInitPolicyEnumByPolicyId(getPolicyId(param)));
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
