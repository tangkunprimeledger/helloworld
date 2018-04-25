package com.higgs.trust.tester.assertutil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import lombok.extern.java.Log;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.sql.Connection;
@Log
public class AssertToolTest extends Assert{

    @Test
    //json相同-断言成功
    public void testIsTotallyEqual1() throws Exception {
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname\":\"id,policy_name\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isTotallyEqual(a,b);
    }

    @Test(expectedExceptions = AssertionError.class)
    //json相同-断言失败-不相等
    public void testIsTotallyEqual2() throws Exception {
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"query1\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname\":\"id,policy_name\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isTotallyEqual(a,b);
    }
    @Test(expectedExceptions = AssertionError.class)
    //json相同-断言失败，expect为空
    public void testIsTotallyEqual3() throws Exception{
        JSONObject a = null;
        String BB = "{\"query1\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname\":\"id,policy_name\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isTotallyEqual(a,b);
    }
    @Test(expectedExceptions = AssertionError.class)
    //JSON相同-断言失败，actual为空
    public void testIsTotallyEqual4()throws Exception{
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = null;
        AssertTool.isTotallyEqual(a,b);
    }

    @Test
    //json包含-断言成功-json相同
    public void testIsContainsExpect1() throws Exception {
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isContainsExpect(b,a);
    }

    @Test
    //json包含-断言成功-actual包含expect
    public void testIsContainsExpect2() throws Exception{
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isContainsExpect(b,a);
    }

    @Test(expectedExceptions = AssertionError.class)
    //json包含-断言失败-actual不包含expect
    public void testIsContainsExpect3() throws Exception{
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key1\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isContainsExpect(b,a);
    }

    @Test(expectedExceptions = AssertionError.class)
    //JSON包含-断言失败，actual为空
    public void testIsContainsExpect4() throws Exception{
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = null;
        AssertTool.isContainsExpect(a,b);
    }

    @Test(expectedExceptions = AssertionError.class)
    //json包含-断言失败，expect为空
    public void testIsContainsExpect5() throws Exception{
        JSONObject a = null;
        String BB = "{\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key1\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isContainsExpect(a,b);
    }

    @Test
    //JSONArray包含-断言成功
    public void testIsContainsExpect6() throws Exception{
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        String expect = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\"}";
        JSONArray actualArray = JSONArray.parseArray(actual);
        JSONObject expectJson = JSONObject.parseObject(expect);
        AssertTool.isContainsExpect(expectJson,actualArray);
    }

    @Test(expectedExceptions = AssertionError.class)
    //JSONArray包含-断言失败-array不包含
    public void testIsContainsExpect7() throws Exception{
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        String expect = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\"}";
        JSONArray actualArray = JSONArray.parseArray(actual);
        JSONObject expectJson = JSONObject.parseObject(expect);
        AssertTool.isContainsExpect(expectJson,actualArray);
    }

    @Test(expectedExceptions = AssertionError.class)
    //JSONArray包含-断言失败-expect为空
    public void testIsContainsExpect8(){
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        JSONArray actualArray = JSONArray.parseArray(actual);
        JSONObject expectJson = null;
        AssertTool.isContainsExpect(expectJson,actualArray);
    }

    @Test(expectedExceptions = AssertionError.class)
    //JSONArray包含-断言失败-actual为空
    public void testIsContainsExpect9(){
        String expect = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\"}";
        JSONObject expectJson = JSONObject.parseObject(expect);
        JSONArray actual = null;
        AssertTool.isContainsExpect(expectJson,actual);
    }

    @Test
    //数据库包含-断言成功
    public void testIsContainsExpect10() throws Exception {
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isContainsExpect(c,array);
    }

    @Test(expectedExceptions = AssertionError.class)
    //数据库包含-断言失败-array不包含
    public void testisContainsExpect11() throws Exception{
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isContainsExpect(c,array);

    }

    @Test(expectedExceptions = NullPointerException.class)
    //数据库包含-断言失败-数据库无法连接
    public void testIsContainsExpect12(){
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3304/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isContainsExpect(c,array);
    }

    @Test(expectedExceptions = AssertionError.class)
    //数据库包含-断言失败-数据库查询结果为空
    public void testIsContainsExpect13(){
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select test from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isContainsExpect(c,array);
    }

    @Test
    //json包含-断言成功-actual包含expect的所有键值对
    public void testiIsContainsExpectJsonNode1(){
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"insert2\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isContainsExpectJsonNode(b,a);
    }

    @Test(expectedExceptions = AssertionError.class)
    ///json包含-断言失败-actual不包含expect的所有的键值对
    public void testiIsContainsExpectJsonNode2(){
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"insert2\":{\"value1\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isContainsExpectJsonNode(b,a);
    }

    @Test(expectedExceptions = AssertionError.class)
    //json包含--断言失败-actual为空
    public void testiIsContainsExpectJsonNode3(){
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = null;
        AssertTool.isContainsExpectJsonNode(a,b);
    }
    @Test(expectedExceptions = AssertionError.class)
    //json包含--断言失败-actual为空
    public void testiIsContainsExpectJsonNode4(){
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = null;
        AssertTool.isContainsExpectJsonNode(b,a);
    }

    @Test(expectedExceptions = AssertionError.class)
    //jsonarray包含node-断言失败-actual不包含expect的所有键值对
    public void testiIsContainsExpectJsonNode5(){
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        String expect = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\"}";
        JSONArray actualArray = JSONArray.parseArray(actual);
        JSONObject expectJson = JSONObject.parseObject(expect);
        AssertTool.isContainsExpectJsonNode(expectJson,actualArray);
    }

    @Test
    ///jsonarray包含node-断言成功-actual包含expect的所有的键值对
    public void testiIsContainsExpectJsonNode6(){
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        String expect = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\"}";
        JSONArray actualArray = JSONArray.parseArray(actual);
        JSONObject expectJson = JSONObject.parseObject(expect);
        AssertTool.isContainsExpectJsonNode(expectJson,actualArray);
    }

    @Test(expectedExceptions = AssertionError.class)
    //jsonarray包含node--断言失败-expect为空
    public void testiIsContainsExpectJsonNode7(){
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        JSONArray actualobj = JSONArray.parseArray(actual);
        JSONObject expect = null;
        AssertTool.isContainsExpectJsonNode(expect,actualobj);
    }
    @Test(expectedExceptions = AssertionError.class)
    //jsonarray包含node--断言失败-actual为空
    public void testiIsContainsExpectJsonNode8(){
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONArray b = null;
        AssertTool.isContainsExpectJsonNode(a,b);
    }

    @Test
    //数据库包含-断言成功
    public void testIsContainsExpectJsonNode10() throws Exception {
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isContainsExpectJsonNode(c,array);
    }

    @Test(expectedExceptions = AssertionError.class)
    //数据库包含-断言失败-array不包含
    public void testisContainsExpectJsonNode11() throws Exception{
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isContainsExpectJsonNode(c,array);

    }

    @Test(expectedExceptions = NullPointerException.class)
    //数据库包含-断言失败-数据库无法连接
    public void testIsContainsExpectJsonNode12(){
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3304/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isContainsExpectJsonNode(c,array);
    }

    @Test(expectedExceptions = AssertionError.class)
    //数据库包含-断言失败-数据库查询结果为空
    public void testIsContainsExpectJsonNode13(){
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select test from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isContainsExpectJsonNode(c,array);
    }

    @Test(expectedExceptions = AssertionError.class)
    //json包含-断言失败-json相同
    public void testIsNotContainsExpect1() throws Exception {
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isNotContainsExpect(b,a);
    }

    @Test(expectedExceptions = AssertionError.class)
    //json包含-断言成功-actual包含expect
    public void testIsNotContainsExpect2() throws Exception{
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isNotContainsExpect(b,a);
    }

    @Test
    //json包含-断言失败-actual不包含expect
    public void testIsNotContainsExpect3() throws Exception{
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"insert1\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key1\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename4\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isNotContainsExpect(b,a);
    }

    @Test(expectedExceptions = AssertionError.class)
    //JSON包含-断言失败，actual为空
    public void testIsNotContainsExpect4() throws Exception{
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = null;
        AssertTool.isNotContainsExpect(a,b);
    }

    @Test(expectedExceptions = AssertionError.class)
    //json包含-断言失败，expect为空
    public void testIsNotContainsExpect5() throws Exception{
        JSONObject a = null;
        String BB = "{\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key1\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isNotContainsExpect(a,b);
    }

    @Test(expectedExceptions = AssertionError.class)
    //JSONArray包含-断言失败-array包含
    public void testIsNotContainsExpect6() throws Exception{
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        String expect = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\"}";
        JSONArray actualArray = JSONArray.parseArray(actual);
        JSONObject expectJson = JSONObject.parseObject(expect);
        AssertTool.isNotContainsExpect(expectJson,actualArray);
    }

    @Test
    //JSONArray包含-断言失败-array不包含
    public void testIsNotContainsExpect7() throws Exception{
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        String expect = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\"}";
        JSONArray actualArray = JSONArray.parseArray(actual);
        JSONObject expectJson = JSONObject.parseObject(expect);
        AssertTool.isNotContainsExpect(expectJson,actualArray);
    }

    @Test(expectedExceptions = AssertionError.class)
    //JSONArray包含-断言失败-expect为空
    public void testIsNotContainsExpect8(){
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        JSONArray actualArray = JSONArray.parseArray(actual);
        JSONObject expectJson = null;
        AssertTool.isNotContainsExpect(expectJson,actualArray);
    }

    @Test(expectedExceptions = AssertionError.class)
    //JSONArray包含-断言失败-actual为空
    public void testIsNotContainsExpect9(){
        String expect = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\"}";
        JSONObject expectJson = JSONObject.parseObject(expect);
        JSONArray actual = null;
        AssertTool.isNotContainsExpect(expectJson,actual);
    }

    @Test(expectedExceptions = AssertionError.class)
    //数据库包含-断言失败-包含
    public void testIsNotContainsExpect10() throws Exception {
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isNotContainsExpect(c,array);
    }

    @Test
    //数据库包含-断言失败-array不包含
    public void testisNotContainsExpect11() throws Exception{
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isNotContainsExpect(c,array);

    }

    @Test(expectedExceptions = NullPointerException.class)
    //数据库包含-断言失败-数据库无法连接
    public void testIsNotContainsExpect12(){
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3304/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isNotContainsExpect(c,array);
    }

    @Test
    //数据库包含-断言失败-数据库查询结果为空
    public void testIsNotContainsExpect13(){
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select test from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isNotContainsExpect(c,array);
    }

    @Test(expectedExceptions = AssertionError.class)
    //json包含-断言失败-actual包含expect的所有键值对
    public void testIsNotContainsExpectJsonNode1(){
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"insert2\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isNotContainsExpectJsonNode(b,a);
    }

    @Test
    ///json包含-断言失败-actual不包含expect的所有的键值对
    public void testiIsNotContainsExpectJsonNode2(){
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        String BB = "{\"insert2\":{\"value1\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = JSON.parseObject(BB);
        AssertTool.isNotContainsExpectJsonNode(b,a);
    }

    @Test(expectedExceptions = AssertionError.class)
    //json包含--断言失败-actual为空
    public void testiIsNotContainsExpectJsonNode3(){
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = null;
        AssertTool.isNotContainsExpectJsonNode(a,b);
    }
    @Test(expectedExceptions = AssertionError.class)
    //json包含--断言失败-actual为空
    public void testiIsNotContainsExpectJsonNode4(){
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONObject b = null;
        AssertTool.isNotContainsExpectJsonNode(b,a);
    }

    @Test
    //jsonarray包含node-断言成功-actual不包含expect的所有键值对
    public void testiIsNotContainsExpectJsonNode5(){
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        String expect = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\"}";
        JSONArray actualArray = JSONArray.parseArray(actual);
        JSONObject expectJson = JSONObject.parseObject(expect);
        AssertTool.isNotContainsExpectJsonNode(expectJson,actualArray);
    }

    @Test(expectedExceptions = AssertionError.class)
    ///jsonarray包含node-断言成功-actual包含expect的所有的键值对
    public void testiIsNotContainsExpectJsonNode6(){
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        String expect = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\"}";
        JSONArray actualArray = JSONArray.parseArray(actual);
        JSONObject expectJson = JSONObject.parseObject(expect);
        AssertTool.isNotContainsExpectJsonNode(expectJson,actualArray);
    }

    @Test(expectedExceptions = AssertionError.class)
    //jsonarray包含node--断言失败-expect为空
    public void testiIsNotContainsExpectJsonNode7(){
        String actual = "[{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"},{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":{\"rs_ids\":\"[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]\",\"policy_id\":\"9344819785\",\"create_time\":\"2018-04-03 17:26:57.0\",\"policy_name\":\"test-policy\",\"id\":\"31\"},\"policy_name\":\"test-policy\",\"id\":\"31\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-2050418221\",\"create_time\":\"2018-04-20 16:36:11.186\",\"policy_name\":\"test-policy\",\"id\":\"33\"},{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"445810278\",\"create_time\":\"2018-04-20 16:45:47.337\",\"policy_name\":\"test-policy\",\"id\":\"35\"},{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"528748308\",\"create_time\":\"2018-04-20 16:49:32.673\",\"policy_name\":\"test-policy\",\"id\":\"37\"}]";
        JSONArray actualobj = JSONArray.parseArray(actual);
        JSONObject expect = null;
        AssertTool.isNotContainsExpectJsonNode(expect,actualobj);
    }
    @Test(expectedExceptions = AssertionError.class)
    //jsonarray包含node--断言失败-actual为空
    public void testiIsNotContainsExpectJsonNode8(){
        String AA = "{\"query\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename1\":\"policy\",\"listname1\":\"id,policy_name\"},\"insert\":{\"value\":\"('11', '934489785', 'test-policy', '[\\\"test1\\\",\\\"test2\\\",\\\"test3\\\"]', '2018-04-03 17:26:57.000')\",\"key\":\"`id`, `policy_id`, `policy_name`, `rs_ids`, `create_time`\",\"tablename2\":\"policy\"},\"delete\":{\"condition\":\"id = 1 && policy_name = 'test-policy'\",\"tablename3\":\"policy\"}}";
        JSONObject a = JSON.parseObject(AA);
        JSONArray b = null;
        AssertTool.isNotContainsExpectJsonNode(a,b);
    }

    @Test(expectedExceptions = AssertionError.class)
    //数据库包含-断言失败-包含
    public void testIsNotContainsExpectJsonNode10() throws Exception {
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isNotContainsExpectJsonNode(c,array);
    }

    @Test
    //数据库包含-断言失败-array不包含
    public void testisNotContainsExpectJsonNode11() throws Exception{
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isNotContainsExpectJsonNode(c,array);

    }

    @Test(expectedExceptions = NullPointerException.class)
    //数据库包含-断言失败-数据库无法连接
    public void testIsNotContainsExpectJsonNode12(){
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3304/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select * from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids1\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isNotContainsExpectJsonNode(c,array);
    }

    @Test(expectedExceptions = AssertionError.class)
    //数据库包含-断言失败-数据库查询结果为空
    public void testIsNotContainsExpectJsonNode13(){
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection connection = dataBaseManager.getMysqlConnection("jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        JSONArray array = dataBaseManager.executeQuery("select test from policy where policy_name = 'test-policy'", connection);
        for (int i = 0; i<array.size();i++){
            System.out.println(array.get(i));
        }
        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        JSONObject c = JSON.parseObject(CC);
        AssertTool.isNotContainsExpectJsonNode(c,array);
    }

    @Test(expectedExceptions = AssertionError.class)
    //封装了数据库操作的断言，数据库查询失败
    public void testIsContainsExpect14()throws Exception{
        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        String dburl = "jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true";
        String sql = "select test from policy where policy_name = 'test-policy'";
        AssertTool.isContainsExpect(CC, dburl,sql);
    }

    @Test
    //封装了数据库操作的断言，数据库包含
    public void testIsContainsExpect15()throws Exception{
        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
        String dburl = "jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true";
        String sql = "select * from policy where policy_name = 'test-policy'";
        AssertTool.isContainsExpect(CC, dburl,sql);
    }
}