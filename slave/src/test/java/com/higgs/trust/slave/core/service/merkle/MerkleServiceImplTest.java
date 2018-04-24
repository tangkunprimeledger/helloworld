package com.higgs.trust.slave.core.service.merkle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.JsonFileUtil;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.MerkleException;
import com.higgs.trust.slave.model.bo.merkle.MerkleNode;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.fail;

/**
 * @author WangQuanzhou
 * @desc test case for  MerkleServiceImpl
 * @date 2018/4/12 16:53
 */
public class MerkleServiceImplTest extends BaseTest {
//public class MerkleServiceImplTest {

    //数据驱动
    @DataProvider public Object[][] provideBuildData(Method method) {
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil
            .jsonFileToArry("./src/test/resources/java/com/higgs/trust/slave/core/service/merkle/testBuild/testRegular");
        return arrmap;
    }

    //数据驱动
    @DataProvider public Object[][] provideBuildExceptionData(Method method) {
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil
            .jsonFileToArry("./src/test/resources/java/com/higgs/trust/slave/core/service/merkle/testBuild/testException");
        return arrmap;
    }

    //数据驱动
    @DataProvider public Object[][] provideUpdateData(Method method) {
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil
            .jsonFileToArry("./src/test/resources/java/com/higgs/trust/slave/core/service/merkle/testUpdate/testRegular");
        return arrmap;
    }

    //数据驱动
    @DataProvider public Object[][] provideUpdateExceptionData(Method method) {
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil
            .jsonFileToArry("./src/test/resources/java/com/higgs/trust/slave/core/service/merkle/testUpdate/testException");
        return arrmap;
    }

    //数据驱动
    @DataProvider public Object[][] provideAddData(Method method) {
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil
            .jsonFileToArry("./src/test/resources/java/com/higgs/trust/slave/core/service/merkle/testAdd/testRegular");
        return arrmap;
    }

    //数据驱动
    @DataProvider public Object[][] provideAddExceptionData(Method method) {
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil
            .jsonFileToArry("./src/test/resources/java/com/higgs/trust/slave/core/service/merkle/testAdd/testException");
        return arrmap;
    }

    @Autowired
    private MerkleService merkleService;

    @Test
    public void initAdd(){
        merkleService.truncateMerkle();
        List tempTxList = new LinkedList();
        tempTxList.add("a");
        tempTxList.add("b");
        tempTxList.add("c");
        tempTxList.add("d");
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        merkleService.flush(merkleTree);
    }

    @Test
    public void initUpdate(){
        merkleService.truncateMerkle();
        List tempTxList = new LinkedList();
        tempTxList.add("a");
        tempTxList.add("b");
        tempTxList.add("c");
        tempTxList.add("d");
        tempTxList.add("e");
        tempTxList.add("m");
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.RS, tempTxList);
        merkleService.flush(merkleTree);
    }

    @Test(dataProvider = "provideBuildData")
    public void testBuild(Map<?, ?> param) throws InterruptedException {
        merkleService.truncateMerkle();
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


    @Test(dataProvider = "provideBuildExceptionData")
    public void testBuildException(Map<?, ?> param) throws InterruptedException {
        merkleService.truncateMerkle();
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
            fail("Expected an slave merkle param not valid exception to be thrown");
        } catch (MerkleException e) {
            assertEquals(e.getMessage(), param.get("assert").toString());
        }
    }

    @Test(dataProvider = "provideUpdateData",dependsOnMethods = { "initUpdate" })
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

    @Test(dataProvider = "provideUpdateExceptionData",dependsOnMethods = { "initUpdate" })
    public void testUpdateException(Map<?, ?> param) throws InterruptedException {
        try {
            MerkleTree merkleTree =  merkleService.queryMerkleTree(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")));
            JSONObject bodyObj = JSON.parseObject(param.get("body").toString());
            Map<String, String> map =
                JSONObject.parseObject(bodyObj.toJSONString(), new TypeReference<Map<String, String>>() {
                });
            merkleService.update(merkleTree,map.get("old"),map.get("new"));
            fail("Expected an slave merkle param not valid exception to be thrown");
        } catch (MerkleException e) {
            assertEquals(e.getMessage(), param.get("assert").toString());
        }
    }

    @Test(dataProvider = "provideAddData",dependsOnMethods = { "initAdd" })
    public void testAdd(Map<?, ?> param) throws InterruptedException {
        MerkleTree merkleTree =  merkleService.queryMerkleTree(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")));
        Object obj = param.get("body");
        merkleService.add(merkleTree,obj);
        assertEquals(merkleTree.getRootHash(), param.get("assert").toString());
        merkleService.flush(merkleTree);
    }

    @Test(dataProvider = "provideAddExceptionData",dependsOnMethods = { "initAdd" })
    public void testAddException(Map<?, ?> param) throws InterruptedException {
        try {
            MerkleTree merkleTree =  merkleService.queryMerkleTree(MerkleTypeEnum.getBizTypeEnumBycode((String)param.get("type")));
            Object obj = param.get("body");
            merkleService.add(merkleTree,obj);
            fail("Expected an slave merkle param not valid exception to be thrown");
        } catch (MerkleException e) {
            assertEquals(e.getMessage(), param.get("assert").toString());
        }
    }

    @Test public void testFlush() {
        merkleService.truncateMerkle();
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


    @Test public void testQueryMerkleTree(){
        MerkleTypeEnum treeType = MerkleTypeEnum.RS;
        assertEquals(merkleService.queryMerkleTree(treeType).getTreeType(),treeType);
    }

    @Test public void testQueryMerkleTreeException(){
            MerkleTree tree = merkleService.queryMerkleTree(null);
            assertEquals(tree,null);
    }
}