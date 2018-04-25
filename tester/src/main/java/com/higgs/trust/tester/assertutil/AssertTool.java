package com.higgs.trust.tester.assertutil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import com.higgs.trust.tester.jsonutil.JsonFileUtil;
import lombok.extern.java.Log;
import org.testng.Assert;
import java.util.*;

import static org.testng.internal.EclipseInterface.*;


/**
 * @author shenqingyan
 * @create 2018/4/17 20:30
 * @desc assert different json
 **/
@Log
public class AssertTool extends Assert{
    /**
     * @desc 比较两个JSONObject是否完全相等的方法
     * @param expect
     * @param actual
     **/

    private static boolean totallyEqual(JSONObject expect, JSONObject actual){
        if(null == expect) {
            fail("expected a null object, but not null found. ");
        }
        if(null == actual) {
            fail("expected not null object, but null found. ");
        }
        return expect.equals(actual);
    }

    /**
     * @desc 比较两个JSONObject是否完全相等的方断言
     * @param expect
     * @param actual
     **/
    public static void isTotallyEqual(JSONObject expect, JSONObject actual){
        if (AssertTool.totallyEqual(expect,actual)){
            log.info("assert true");
        }else {
            fail(format(actual, expect, "assert failed"));
        }
    }


    /**
     * @desc 判断实际json 包含期望json值方法
     * @param expect
     * @param actual
     **/
    private static boolean containsExpect(JSONObject expect, JSONObject actual){
        HashMap<String, Object> exmap = new HashMap<>();
        HashMap<String, Object> acmap = new HashMap<>();
        HashMap<String, Object> tmp = new HashMap<>();
        exmap = JsonFileUtil.jsonToMap(expect,exmap);
        acmap = JsonFileUtil.jsonToMap(actual, acmap);
        int sum = 0;
        for (String key: exmap.keySet()){
            if (acmap.keySet().contains(key) && exmap.get(key).equals(acmap.get(key))){
                sum += 1;
            }
        }
        if (sum == exmap.size()){
            log.info("is contains");
            return true;
        }else {
            log.info("not contains");
            return false;
        }
    }

    /**
     * @desc 判断实际json 包含期望json值断言
     * @param expect
     * @param actual
     **/
    public static void isContainsExpect(JSONObject expect, JSONObject actual){
        if(null == expect) {
            fail("expected a null JsonObject, but not null found. " );
        }
        if(null == actual) {
            fail("expected not null JsonObject, but null found. ");
        }
        if (AssertTool.containsExpect(expect,actual)){
            log.info("assert true");
        }else {
            fail(format(actual, expect, "assert failed"));
        }
    }

    /**
     * @desc 判断实际JSONArray 是否包含 某个json方法
     * @param expect
     * @param actual
     **/
    private static boolean containsExpect(JSONObject expect, JSONArray actual){
        boolean result = false;
        if (actual.size() > 0){
            for (int i = 0; i< actual.size(); i++){
                JSONObject acobj = actual.getJSONObject(i);
                if (containsExpect(expect, acobj)){
                    result = true;
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * @desc 判断实际JSONArray 是否包含 某个json断言
     * @param expect
     * @param actual
     **/
    public static void isContainsExpect(JSONObject expect, JSONArray actual){
        if(null == expect) {
            fail("expected a null JsonObject, but not null found. ");
        }
        if(null == actual || actual.size() == 0) {
            fail("expected not null JsonObject, but null found. ");
        }
        if (AssertTool.containsExpect(expect,actual)){
            log.info("assert true");
        }else {
            fail(format(actual, expect, "assert failed"));
        }
    }


    /**
     * @desc 判断实际json 包含期望json NODE节点的键值对方法
     * @param expect
     * @param actual
     **/
    private static boolean containsExpectJsonNode(JSONObject expect, JSONObject actual){
        HashMap<String, Object> exmap = new HashMap<>();
        HashMap<String, Object> acmap = new HashMap<>();
        exmap = JsonFileUtil.jsonNodeToMap(expect,exmap);
        acmap = JsonFileUtil.jsonNodeToMap(actual, acmap);
        int sum = 0;
        for (String key: exmap.keySet()){
            if (acmap.keySet().contains(key) && exmap.get(key).equals(acmap.get(key))){
                sum += 1;
            }
        }
        if (sum == exmap.size()){
            log.info("is contains");
            return true;
        }else {
            log.info("not contains、");
            return false;
        }
    }

    /**
     * @desc 判断实际json 包含期望json NODE节点的键值对断言
     * @param expect
     * @param actual
     **/
    public static void isContainsExpectJsonNode(JSONObject expect, JSONObject actual){
        if(null == expect) {
            fail("expected a null JsonObject, but not null found. ");
        }
        if(null == actual) {
            fail("expected not null JsonObject, but null found. " );
        }
        if (AssertTool.containsExpectJsonNode(expect,actual)){
            log.info("assert true");
        }else {
            fail(format(actual, expect, "assert failed"));
        }
    }

    /**
     * @desc 判断实际jsonarray 包含期望json NODE节点的键值对方法
     * @param expect
     * @param actual
     **/
    private static boolean containsExpectJsonNode(JSONObject expect, JSONArray actual){
        boolean result = false;
        if (actual.size() > 0){
            for (int i = 0; i< actual.size(); i++){
                JSONObject acobj = actual.getJSONObject(i);
                if (containsExpectJsonNode(expect, acobj)){
                    result = true;
                    return result;
                }
            }
        }else {
            log.info("jsonarray为空");
        }
        return result;
    }

    /**
     * @desc 判断实际jsonarray 包含期望json NODE节点的键值对断言
     * @param expect
     * @param actual
     **/
    public static void isContainsExpectJsonNode(JSONObject expect, JSONArray actual){
        if(null == expect) {
            fail("expected a null JsonObject, but not null found. ");
        }
        if(null == actual || actual.size() == 0) {
            fail("expected not null JsonObject, but null found. ");
        }
        if (AssertTool.containsExpectJsonNode(expect,actual)){
            log.info("assert true");
        }else {
            fail(format(actual, expect, "assert failed"));
        }
    }

    /**
     * @desc 判断实际数据库 包含期望json NODE节点的键值对断言
     * @param expect
     * @param dburl
     * @param sql
     **/
    private static boolean containsExpectJsonNode(String expect, String dburl, String sql){
        DataBaseManager dataBaseManager = new DataBaseManager();
        JSONArray array = dataBaseManager.executeSingleQuery(sql,dburl);
        if(array.size() == 0) {
            fail("expected not null JsonObject, but null found. ");
        }
        for (int i = 0; i<array.size();i++){
            log.info(array.get(i).toString());
        }
        JSONObject expjson = JSON.parseObject(expect);
        return containsExpectJsonNode(expjson,array);
    }
    /**
     * @desc 判断数据库结果是否包含预期json子键值对断言
     * @param expect
     * @param dburl
     * @param sql
     **/
    public static void isContainsExpectJsonNode(String expect, String dburl, String sql){

        if (AssertTool.containsExpectJsonNode(expect,dburl,sql)){
            log.info("assert true");
        }else {
            fail(format(" ", expect, "assert failed"));
        }
    }
    /**
     * @desc 判断实际string包含期望string值方法
     * @param expect
     * @param actual
     **/
    private static boolean containsExpect(String expect, String actual){
        return actual.contains(expect);
    }

    /**
     * @desc 判断实际string包含期望string值断言
     * @param expect
     * @param actual
     **/
    public static void isContainsExpect(String expect, String actual){
        if(null == expect) {
            fail("expected a null JsonObject, but not null found. ");
        }
        if(null == actual) {
            fail("expected not null JsonObject, but null found. ");
        }
        if (AssertTool.containsExpect(expect,actual)){
            log.info("assert true");
        }else {
            fail(format(actual, expect, "assert failed"));
        }
    }

    /**
     * @desc 判断数据库结果是否包含预期json方法
     * @param expect
     * @param dburl
     * @param sql
     **/
    private static boolean containsExpect(String expect, String dburl, String sql){
        DataBaseManager dataBaseManager = new DataBaseManager();
        JSONArray array = dataBaseManager.executeSingleQuery(sql,dburl);
        if(array == null || array.size() == 0) {
            fail("expected not null JsonObject, but null found. ");
        }
        for (int i = 0; i<array.size();i++){
            log.info(array.get(i).toString());
        }
        JSONObject expjson = JSON.parseObject(expect);
        return containsExpect(expjson,array);
        }


    /**
     * @desc 判断数据库结果是否包含预期json断言
     * @param expect
     * @param dburl
     * @param sql
     **/
    public static void isContainsExpect(String expect, String dburl, String sql){

        if (AssertTool.containsExpect(expect,dburl,sql)){
            log.info("assert true");
        }else {
            fail(format(" ", expect, "assert failed"));
        }
    }
    /**
     * @desc 判断实际json不包含期望json方法
     * @param expect
     * @param actual
     **/
    private static boolean notContainsExpect(JSONObject expect, JSONObject actual) {
        HashMap<String, Object> exmap = new HashMap<>();
        HashMap<String, Object> acmap = new HashMap<>();
        exmap = JsonFileUtil.jsonToMap(expect, exmap);
        acmap = JsonFileUtil.jsonToMap(actual, acmap);
        int sum = 0;
        for (String key : exmap.keySet()) {
            if (acmap.keySet().contains(key) && exmap.get(key).equals(acmap.get(key))) {
                sum += 1;
            }
        }
        if (sum != exmap.size()) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * @desc 判断实际jsonnode不包含期望json方法
     * @param expect
     * @param actual
     **/
    private static boolean notContainsExpectNode(JSONObject expect, JSONObject actual) {
        HashMap<String, Object> exmap = new HashMap<>();
        HashMap<String, Object> acmap = new HashMap<>();
        exmap = JsonFileUtil.jsonNodeToMap(expect, exmap);
        acmap = JsonFileUtil.jsonNodeToMap(actual, acmap);
        int sum = 0;
        for (String key : exmap.keySet()) {
            if (acmap.keySet().contains(key) && exmap.get(key).equals(acmap.get(key))) {
                sum =sum + 1;
            }
        }
        if (sum != exmap.size()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * @desc 判断实际json不包含期望json断言
     * @param expect
     * @param actual
     **/
    public static void isNotContainsExpect(JSONObject expect, JSONObject actual){
        if(null == expect) {
            fail("expected is a null JsonObject ");
        }
        if(null == actual) {
            fail("actual is a null JsonObject");
        }
        if (AssertTool.notContainsExpect(expect,actual)){
            log.info("assert true");
        }else {
            failSame(actual,expect,"assert failed");
        }
    }

    /**
     * @desc 判断实际json不包含期望jsonNODE断言
     * @param expect
     * @param actual
     **/
    public static void isNotContainsExpectJsonNode(JSONObject expect, JSONObject actual){
        if(null == expect) {
            fail("expected is a null JsonObject ");
        }
        if(null == actual) {
            fail("actual is a null JsonObject");
        }
        if (AssertTool.notContainsExpectNode(expect,actual)){
            log.info("assert true");
        }else {
            failSame(actual,expect,"assert failed");
        }
    }

    /**
     * @desc 判断实际jsonarry不包含期望json方法
     * @param expect
     * @param actual
     **/
    private static boolean notContainsExpect(JSONObject expect, JSONArray actual){
        int sum = 0;
        if (actual.size()> 0){
            for (int i = 0; i< actual.size();i++){
                JSONObject object = actual.getJSONObject(i);
                if (notContainsExpect(expect, object)){
                    sum +=1;
                }
            }
        }
        if (sum == actual.size()){
            return true;
        }else {
            return false;
        }

    }

    /**
     * @desc 判断实际jsonarry不包含期望jsonNODE方法
     * @param expect
     * @param actual
     **/
    private static boolean notContainsExpectNode(JSONObject expect, JSONArray actual){
        int sum = 0;
        if (actual.size()> 0){
            for (int i = 0; i< actual.size();i++){
                JSONObject object = actual.getJSONObject(i);
                if (notContainsExpectNode(expect, object)){
                    sum +=1;
                }
            }
        }
        if (sum == actual.size()){
            return true;
        }else {
            return false;
        }
    }

    /**
     * @desc 判断实际jsonarry不包含期望json
     * @param expect
     * @param actual
     **/
    public static void isNotContainsExpect(JSONObject expect, JSONArray actual){
        if(null == expect) {
            fail("expected is a null JsonObject ");
        }
        if(null == actual) {
            fail("actual is a null JsonObject");
        }
        if (AssertTool.notContainsExpect(expect,actual)){
            log.info("assert true");
        }else {
            failSame(actual,expect,"assert failed");
        }
    }


    /**
     * @desc 判断实际jsonarry不包含期望json
     * @param expect
     * @param actual
     **/
    public static void isNotContainsExpectJsonNode(JSONObject expect, JSONArray actual){
        if(null == expect) {
            fail("expected is a null JsonObject ");
        }
        if(null == actual || actual.size() == 0) {
            fail("actual is a null JsonObject");
        }
        if (AssertTool.notContainsExpectNode(expect,actual)){
            log.info("assert true");
        }else {
            failSame(actual,expect,"assert failed");
        }
    }

    /**
     * @desc 判断数据库中是否不包含预期json方法
     * @param expect
     * @param dburl
     * @param sql
     **/
    private static boolean notContainsExpect(String expect,String dburl, String sql){
        DataBaseManager dataBaseManager = new DataBaseManager();
        JSONArray array = dataBaseManager.executeSingleQuery(sql,dburl);
        if(null == array || array.size() == 0) {
            fail("actual is a null JsonObject");
        }
        for (int i = 0; i<array.size();i++){
            log.info(array.get(i).toString());
        }
        JSONObject expjson = JSON.parseObject(expect);
        return notContainsExpect(expjson, array);
    }

    /**
     * @desc 判断数据库中是否不包含预期jsonnode方法
     * @param expect
     * @param dburl
     * @param sql
     **/
    private static boolean notContainsExpectNode(String expect,String dburl, String sql){
        DataBaseManager dataBaseManager = new DataBaseManager();
        JSONArray array = dataBaseManager.executeSingleQuery(sql,dburl);
        if(null == array || array.size() == 0) {
            fail("actual is a null JsonObject");
        }
        for (int i = 0; i<array.size();i++){
            log.info(array.get(i).toString());
        }
        JSONObject expjson = JSON.parseObject(expect);
        return notContainsExpectNode(expjson, array);
    }

    /**
     * @desc 判断数据库中是否不包含预期json断言
     * @param expect
     * @param dburl
     * @param sql
     **/
    public static void isNotContainsExpect(String expect,String dburl, String sql){

        if (AssertTool.notContainsExpect(expect,dburl,sql)){
            log.info("assert true");
        }else {
            fail("assert failed");
        }
    }

    /**
     * @desc 判断数据库中是否不包含预期jsonnode断言
     * @param expect
     * @param dburl
     * @param sql
     **/
    public static void isNotContainsExpectJsonNode(String expect,String dburl, String sql){
        if (AssertTool.notContainsExpectNode(expect,dburl,sql)){
            log.info("assert true");
        }else {
            fail("assert failed");
        }
    }

    /**
     * @desc 格式化异常信息
     * @param message
     * @param expect
     * @param actual
     **/
    private static String format(Object actual, Object expect, String message) {
        String formatted = "";
        if (null != message) {
            formatted = message + " ";
        }
        return formatted + ASSERT_LEFT + expect + ASSERT_MIDDLE + actual + ASSERT_RIGHT;
    }

    static private void failSame(Object actual, Object expected, String message) {
        String formatted = "";
        if(message != null) {
            formatted = message + " ";
        }
        fail(formatted + ASSERT_LEFT2 + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT);
    }
}
