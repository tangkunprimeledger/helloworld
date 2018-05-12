package com.higgs.trust.slave.core.service.action.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.JsonFileUtil;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author duhongming
 * @date 2018/5/2
 */
public abstract class ContractBaseTest extends BaseTest {

    @Autowired SnapshotService snapshot;

    private static String toLowerCaseFirstChar(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    protected String getDbConnectString() {
        try {
            String json = IOUtils.toString(this.getClass().getResource("/test-application.json"), "UTF-8");
            JSONObject config = (JSONObject) JSON.parse(json);
            JSONObject dbConf = config.getJSONObject("spring").getJSONObject("datasource").getJSONObject("druid");
            String connectStr = dbConf.getString("url");// "jdbc:mysql://localhost:3306/trust?user=root&password=root";
            if (connectStr.indexOf("user=") > 0) {
                return connectStr;
            }

            connectStr = String.format("%s&user=%s&password=%s", connectStr, dbConf.getString("username"), dbConf.getString("password"));
            return connectStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void doTestValidate(Map<?, ?> param, PackContext packContext, ActionHandler handler) {
        try {
            handler.validate(packContext);
        } catch (SlaveException ex) {
            ex.printStackTrace();
            Assert.assertEquals(ex.getMessage(), String.valueOf(param.get("assert")));
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.assertEquals(ex.getMessage(), String.valueOf(param.get("assert")));
        }
    }

    public void doTestPersist(Map<?, ?> param, PackContext packContext, ActionHandler handler) {
        try {
            handler.persist(packContext);
        } catch (SlaveException ex) {
            ex.printStackTrace();
            Assert.assertEquals(ex.getMessage(), String.valueOf(param.get("assert")));
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.assertEquals(ex.getMessage(), String.valueOf(param.get("assert")));
        }
    }

    @DataProvider
    public Object[][] defaultProvider(Method method) {
        String providerPath = getProviderRootPath();
        return getProviderData(providerPath);
    }

    @DataProvider
    public Object[][] byMethodNameProvider(Method method) {
        String name = method.getName();
        name = name.replace("test", "");
        name = toLowerCaseFirstChar(name);

        String providerPath = String.format("%s%s",  getProviderRootPath(), name);
        return getProviderData(providerPath);
    }

    protected Object[][] getProviderData(String providerPath) {
        String filePath = JsonFileUtil.findJsonFile(providerPath);
        HashMap<String, String>[][] arrMap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filePath);
        return arrMap;
    }

    protected <T> T getBody(Map<?, ?> param, Class<T> clazz) {
        String bodyJson = String.valueOf(param.get("body"));
        if (StringUtils.isEmpty(bodyJson) || StringUtils.equals(bodyJson, "null")) {
            return null;
        }
        bodyJson = bodyJson.replaceAll("\"@type\":\"com.alibaba.fastjson.JSONObject\",","");
        return JSON.parseObject(bodyJson, clazz);
    }

    protected void executeDelete(String sql) {
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection conn = dataBaseManager.getMysqlConnection(getDbConnectString());
        dataBaseManager.executeDelete(sql, conn);
    }

    protected List<JSONArray> executeSql(List<String> sqls) {
        if (CollectionUtils.isEmpty(sqls)) {
            return null;
        }
        List<JSONArray> queryResult = new ArrayList<>();
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection conn = dataBaseManager.getMysqlConnection(getDbConnectString());
        for (String sql : sqls) {
            if (sql.toUpperCase().contains("INSERT")) {
                dataBaseManager.executeInsert(sql, conn);
            } else if (sql.toUpperCase().contains("UPDATE")) {
                dataBaseManager.executeUpdate(sql, conn);
            } else if (sql.toUpperCase().contains("DELETE") || sql.toUpperCase().contains("TRUNCATE")) {
                executeDelete(sql);
            } else if (sql.toUpperCase().contains("SELECT")) {
                JSONArray jsonArray = dataBaseManager.executeQuery(sql,conn);
                queryResult.add(jsonArray);
            }
        }
        dataBaseManager.closeConnection(conn);
        return queryResult;
    }

    public abstract String getProviderRootPath();
}
