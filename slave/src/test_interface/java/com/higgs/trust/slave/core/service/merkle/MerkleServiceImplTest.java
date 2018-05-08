package com.higgs.trust.slave.core.service.merkle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.higgs.trust.slave.JsonFileUtil;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.common.exception.MerkleException;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.fail;

/**
 * @author WangQuanzhou
 * @desc test case for  MerkleServiceImpl
 * @date 2018/4/12 16:53
 */
public class MerkleServiceImplTest extends BaseTest {

    DataBaseManager dataBaseManager = new DataBaseManager();
    String url="jdbc:mysql://localhost:3306/trust?user=root&password=root&useUnicode=true&characterEncoding=UTF8&allowMultiQueries=true&useAffectedRows=true";
    String sql = "truncate table merkle_node;truncate table merkle_tree;";

    //数据驱动
    @DataProvider public Object[][] provideBuildData(Method method) {
        String filepath = JsonFileUtil.findJsonFile("java/com/higgs/trust/slave/core/service/merkle/testBuild/testRegular");
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filepath);
        return arrmap;
    }

    //数据驱动
    @DataProvider public Object[][] provideBuildExceptionData(Method method) {
        String filepath = JsonFileUtil.findJsonFile("java/com/higgs/trust/slave/core/service/merkle/testBuild/testException");
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filepath);
        return arrmap;
    }

    //数据驱动
    @DataProvider public Object[][] provideUpdateData(Method method) {
        String filepath = JsonFileUtil.findJsonFile("java/com/higgs/trust/slave/core/service/merkle/testUpdate/testRegular");
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filepath);
        return arrmap;
    }

    //数据驱动
    @DataProvider public Object[][] provideUpdateExceptionData(Method method) {
        String filepath = JsonFileUtil.findJsonFile("java/com/higgs/trust/slave/core/service/merkle/testUpdate/testException");
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filepath);
        return arrmap;
    }

    //数据驱动
    @DataProvider public Object[][] provideAddData(Method method) {
        String filepath = JsonFileUtil.findJsonFile("java/com/higgs/trust/slave/core/service/merkle/testAdd/testRegular");
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filepath);
        return arrmap;
    }

    //数据驱动
    @DataProvider public Object[][] provideAddExceptionData(Method method) {
        String filepath = JsonFileUtil.findJsonFile("java/com/higgs/trust/slave/core/service/merkle/testAdd/testException");
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filepath);
        return arrmap;
    }

    @Autowired
    private MerkleService merkleService;

    @Test(dataProvider = "provideBuildData",priority = 0)
    public void testBuild(Map<?, ?> param) throws InterruptedException {
        dataBaseManager.executeSingleDelete(sql,url);
        JSONObject bodyObj = JSON.parseObject(param.get("body").toString());
        Map<String, String> map =
            JSONObject.parseObject(bodyObj.toJSONString(), new TypeReference<Map<String, String>>() {
            });
        List<Object> dataList = new LinkedList<>();
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            dataList.add(entry.getValue());
        }

        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")), dataList);
        assertEquals(merkleTree.getRootHash(), param.get("assert").toString());
        merkleService.flush(merkleTree);
    }


    @Test(dataProvider = "provideBuildExceptionData",priority = 10)
    public void testBuildException(Map<?, ?> param) throws InterruptedException {
        dataBaseManager.executeSingleDelete(sql,url);
        JSONObject bodyObj = JSON.parseObject(param.get("body").toString());
        Map<String, String> map =
            JSONObject.parseObject(bodyObj.toJSONString(), new TypeReference<Map<String, String>>() {
            });
        List<Object> dataList = new LinkedList<>();
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            dataList.add(entry.getValue());
        }

        try {
            MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")), dataList);
            fail("Expected a slave merkle param not valid exception to be thrown");
        } catch (MerkleException e) {
            assertEquals(e.getMessage(), param.get("assert").toString());
        }
    }

    @Test(priority = 20)
    public void initUpdate(){
        dataBaseManager.executeSingleDelete(sql,url);
        List tempTxList = new LinkedList();
        tempTxList.add("a");
        tempTxList.add("b");
        tempTxList.add("c");
        tempTxList.add("d");
        tempTxList.add("e");
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        merkleService.flush(merkleTree);
    }

    @Test(dataProvider = "provideUpdateData",priority = 30)
    public void testUpdate(Map<?, ?> param) throws InterruptedException {
        MerkleTree merkleTree =  merkleService.queryMerkleTree(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")));
        JSONObject bodyObj = JSON.parseObject(param.get("body").toString());
        Map<String, String> map =
            JSONObject.parseObject(bodyObj.toJSONString(), new TypeReference<Map<String, String>>() {
            });
        merkleService.update(merkleTree,map.get("old"),map.get("new"));
        assertEquals(merkleTree.getRootHash(), param.get("assert").toString());
        merkleService.flush(merkleTree);
    }

    @Test(priority = 40)
    public void initUpdate1(){
        dataBaseManager.executeSingleDelete(sql,url);
        List tempTxList = new LinkedList();
        tempTxList.add("a");
        tempTxList.add("b");
        tempTxList.add("c");
        tempTxList.add("d");
        tempTxList.add("e");
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        merkleService.flush(merkleTree);
    }

    @Test(dataProvider = "provideUpdateExceptionData",priority = 50)
    public void testUpdateException(Map<?, ?> param) throws InterruptedException {
        try {
            MerkleTree merkleTree =  merkleService.queryMerkleTree(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")));
            JSONObject bodyObj = JSON.parseObject(param.get("body").toString());
            Map<String, String> map =
                JSONObject.parseObject(bodyObj.toJSONString(), new TypeReference<Map<String, String>>() {
                });
            merkleService.update(merkleTree,map.get("old"),map.get("new"));
            fail("Expected a slave merkle param not valid exception to be thrown");
        } catch (MerkleException e) {
            assertEquals(e.getMessage(), param.get("assert").toString());
        }
    }

    @Test(priority = 60)
    public void initAdd(){
        dataBaseManager.executeSingleDelete(sql,url);
        List tempTxList = new LinkedList();
        tempTxList.add("a");
        tempTxList.add("b");
        tempTxList.add("c");
        tempTxList.add("d");
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        merkleService.flush(merkleTree);
    }

    @Test(dataProvider = "provideAddData",priority = 70)
    public void testAdd(Map<?, ?> param) throws InterruptedException {
        MerkleTree merkleTree =  merkleService.queryMerkleTree(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")));
        Object obj = param.get("body");
        merkleService.add(merkleTree,obj);
        assertEquals(merkleTree.getRootHash(), param.get("assert").toString());
        merkleService.flush(merkleTree);
    }

    @Test(priority = 80)
    public void initAdd1(){
        dataBaseManager.executeSingleDelete(sql,url);
        List tempTxList = new LinkedList();
        tempTxList.add("a");
        tempTxList.add("b");
        tempTxList.add("c");
        tempTxList.add("d");
        tempTxList.add("e");
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        merkleService.flush(merkleTree);
    }

    @Test(dataProvider = "provideAddData",priority = 90)
    public void testAdd1(Map<?, ?> param) throws InterruptedException {
        MerkleTree merkleTree =  merkleService.queryMerkleTree(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")));
        Object obj = param.get("body");
        merkleService.add(merkleTree,obj);
        assertEquals(merkleTree.getRootHash(), param.get("assert1").toString());
        merkleService.flush(merkleTree);
    }

    @Test(priority = 100)
    public void initAdd2(){
        dataBaseManager.executeSingleDelete(sql,url);
        List tempTxList = new LinkedList();
        tempTxList.add("a");
        tempTxList.add("b");
        tempTxList.add("c");
        tempTxList.add("d");
        tempTxList.add("e");
        tempTxList.add("f");
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        merkleService.flush(merkleTree);
    }

    @Test(dataProvider = "provideAddData",priority = 110)
    public void testAdd2(Map<?, ?> param) throws InterruptedException {
        MerkleTree merkleTree =  merkleService.queryMerkleTree(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")));
        Object obj = param.get("body");
        merkleService.add(merkleTree,obj);
        assertEquals(merkleTree.getRootHash(), param.get("assert2").toString());
        merkleService.flush(merkleTree);
    }

    @Test(priority = 120)
    public void initAdd3(){
        dataBaseManager.executeSingleDelete(sql,url);
        List tempTxList = new LinkedList();
        tempTxList.add("a");
        tempTxList.add("b");
        tempTxList.add("c");
        tempTxList.add("d");
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        merkleService.flush(merkleTree);
    }

    @Test(dataProvider = "provideAddExceptionData",priority = 130)
    public void testAddException(Map<?, ?> param) throws InterruptedException {
        try {
            MerkleTree merkleTree =  merkleService.queryMerkleTree(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")));
            Object obj = param.get("body");
            merkleService.add(merkleTree,obj);
            fail("Expected a slave merkle param not valid exception to be thrown");
        } catch (MerkleException e) {
            assertEquals(e.getMessage(), param.get("assert").toString());
        }
    }

    @Test(priority = 140)
    public void testFlush() {
        dataBaseManager.executeSingleDelete(sql,url);
        List tempTxList = new LinkedList();
        tempTxList.add("a");
        tempTxList.add("b");
        tempTxList.add("c");
        tempTxList.add("d");
        tempTxList.add("e");
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        merkleService.flush(merkleTree);
        merkleTree = merkleService.queryMerkleTree(merkleTree.getTreeType());
        merkleService.update(merkleTree,"c","x");
        merkleService.flush(merkleTree);
        merkleTree = merkleService.queryMerkleTree(merkleTree.getTreeType());
        merkleService.add(merkleTree,"y");

        tempTxList.set(tempTxList.indexOf("c"),"x");
        tempTxList.add("y");
        assertEquals(merkleService.build(MerkleTypeEnum.RS,tempTxList).getRootHash(),merkleTree.getRootHash());

    }


    @Test(priority = 150)
    public void testQueryMerkleTree(){
        MerkleTypeEnum treeType = MerkleTypeEnum.RS;
        assertEquals(merkleService.queryMerkleTree(treeType).getTreeType(),treeType);
    }

    @Test(priority = 160)
    public void testQueryMerkleTreeException(){
        try{
            MerkleTree tree = merkleService.queryMerkleTree(null);
        }catch (MerkleException e){
            assertEquals(e.getMessage(),"slave merkle param not valid exception[SLAVE_MERKLE_PARAM_NOT_VALID_EXCEPTION]");
        }
    }

    @Test
    public void verify(){
        dataBaseManager.executeSingleDelete(sql,url);
        List tempTxList = new LinkedList();
        tempTxList.add("a");
        tempTxList.add("b");
        tempTxList.add("c");
        tempTxList.add("d");
        tempTxList.add("e");
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        merkleService.update(merkleTree,"c","x");
        System.out.println("===========   "+merkleTree.getRootHash());
        merkleService.update(merkleTree,"a","y");
        System.out.println("===========   "+merkleTree.getRootHash());
        merkleService.update(merkleTree,"e","z");
        System.out.println("===========   "+merkleTree.getRootHash());
        tempTxList.set(tempTxList.indexOf("c"),"x");
        tempTxList.set(tempTxList.indexOf("a"),"y");
        tempTxList.set(tempTxList.indexOf("e"),"z");
        MerkleTree merkleTree1 = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        System.out.println("===========   "+merkleTree1.getRootHash());
        assertEquals(merkleTree1.getRootHash(),merkleTree.getRootHash());
    }

    @Test
    public void verify1(){
        dataBaseManager.executeSingleDelete(sql,url);
        try{
            List tempTxList = new LinkedList();
            tempTxList.add("a");
            tempTxList.add("b");
            MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
            String tempSql = "INSERT INTO `merkle_node` VALUES (1, '439431967390302209', 'ad3bbd0236cee779a7b660177b3e7cffb90e915fbc0305e50ccfc64c1f445e47', 0, 2, NULL, 'RS', '2018-4-27 14:25:45', '2018-4-27 14:25:45');";
            dataBaseManager.executeSingleInsert(tempSql,url);
            merkleService.flush(merkleTree);
        }catch (MerkleException e){
            assertEquals(e.getMessage(), "slave merkle node add idempotent exception[SLAVE_MERKLE_NODE_ADD_IDEMPOTENT_EXCEPTION]");
        }
    }

    @Test
    public void verify2(){
        dataBaseManager.executeSingleDelete(sql,url);
        try{
            List tempTxList = new LinkedList();
            tempTxList.add("a");
            tempTxList.add("a");
            MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        }catch (MerkleException e){
            assertEquals(e.getMessage(), "slave merkle node build duplicate exception[SLAVE_MERKLE_NODE_BUILD_DUPLICATE_EXCEPTION]");
        }
    }
}