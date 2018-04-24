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
       // AssertTool.isContainsExpect(c,array);
        String X = "--";
        String B = X.replaceAll("--","null");
        System.out.println(B);
    }

    @Test
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





//    @Test
//    //封装了数据库操作的断言，数据库包含
//    public void testIsContainsExpect3()throws Exception{
//        String CC = "{\"rs_ids\":\"[\\\"test2\\\",\\\"test3\\\",\\\"test1\\\"]\",\"policy_id\":\"-1497234849\",\"create_time\":\"2018-04-20 11:26:38.676\",\"policy_name\":\"test-policy\",\"id\":\"3\"}";
//        String dburl = "jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true";
//        String sql = "select * from policy where policy_name = 'test-policy'";
//        AssertTool.isContainsExpect(CC, dburl,sql);
//    }
}