package com.higgs.trust.slave._interface.snapshot;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.LoadingCache;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.JsonFileUtil;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotServiceImpl;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.dao.po.contract.ContractPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.*;

public class SnapshotServiceImplTest extends BaseTest {

    @Autowired
    private SnapshotServiceImpl snapshotService;

    //数据驱动
    @DataProvider
    public Object[][] initData(Method method) {
        String filepath = JsonFileUtil.findJsonFile("java/com/higgs/trust/slave/core/service/snapshot/init");
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filepath);
        return arrmap;
    }

    //数据驱动
    @DataProvider
    public Object[][] destroyData(Method method) {
        String filepath = JsonFileUtil.findJsonFile("java/com/higgs/trust/slave/core/service/snapshot/destroy");
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filepath);
        return arrmap;
    }

    /**
     * packageCache中是否含有key为SnapshotBizKeyEnum.UTXO类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.MERKLE_TREE类型缓存对象
     * packageCache中是否有key为SnapshotBizKeyEnum.MANAGE类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.DATA_IDENTITY类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.CONTRACT类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.FREEZE类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.CONTRACT类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.CONTRACT_CONTRACT_BIND类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.CONTRACT_SATE类型缓存对象
     * @param params
     */
    @Test(dataProvider = "initData",priority = 1)
    public void testInit(Map<?,?> params) {
        snapshotService.init();
        String key = params.get("key").toString();
        SnapshotBizKeyEnum keyEnum = SnapshotBizKeyEnum.valueOf(key);
        ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> packageCache = getPackageCache();
        assertTrue(packageCache.containsKey(keyEnum));
    }

    /**
     * init执行完之后检查txCache是否为空
     */
    @Test(priority = 2)
    public void testInitTxCache() {
        snapshotService.init();
        ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>> txCache = getTxCache();
        assertTrue(txCache.isEmpty());
    }

    /**
     * init执行完之后检查packageCache是否为空
     */
    @Test(priority = 3)
    public void testInitPackageCache() {
        snapshotService.init();
        ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> packageCache = getPackageCache();
        assertFalse(packageCache.isEmpty());
    }

    /**
     * isOpenTransaction为true的情形，应该会抛出异常
     */
    @Test(priority = 4)
    public void testStartTransaction() {
        snapshotService.init();
        setIsOpenTransaction(true);
        try {
            snapshotService.startTransaction();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot transaction has started exception[SLAVE_SNAPSHOT_TRANSACTION_HAS_STARTED_EXCEPTION]");
        }
    }

    /**
     * 方法执行完成后，isOpenTransaction是否为true
     * @throws Exception
     */
    @Test(priority = 5)
    public void testStartTransaction1() throws Exception {
        snapshotService.init();
        setIsOpenTransaction(false);
        snapshotService.startTransaction();
        assertTrue(getIsOpenTransaction());
    }

    /**
     * 方法执行完成后，txCache是否为空（map为空）
     */
    @Test(priority = 6)
    public void testStartTransaction2() {
        snapshotService.init();
        setIsOpenTransaction(false);
        //TODO 先put数据，不commit，再startTransaction
        snapshotService.startTransaction();
        ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>> txCache = getTxCache();
        assertTrue(txCache.isEmpty());
    }

    /**
     * key1为null
     */
    @Test(priority = 7)
    public void testPut() {
        snapshotService.init();
        setIsOpenTransaction(true);
        try {
            snapshotService.put(null, new ContractSnapshotAgent.ContractCacheKey(), "test");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot  null point  exception[SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION]");
        }
    }

    /**
     * key2为null
     */
    @Test(priority = 7)
    public void testPut1() {
        snapshotService.init();
        setIsOpenTransaction(true);
        try {
            snapshotService.put(SnapshotBizKeyEnum.CONTRACT, null, "test");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot  null point  exception[SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION]");
        }
    }

    /**
     * value为null
     */
    @Test(priority = 7)
    public void testPut2() {
        snapshotService.init();
        setIsOpenTransaction(true);
        try {
            snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "test");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot  null point  exception[SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION]");
        }
    }

    /**
     * isOpenTransaction为false的情形
     */
    @Test(priority = 7)
    public void testPut3() {
        snapshotService.init();
        setIsOpenTransaction(false);
        try {
            snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "test");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot transaction not started exception[SLAVE_SNAPSHOT_TRANSACTION_NOT_STARTED_EXCEPTION]");
        }
    }

    /**
     * txCache已经包含key为key1的对象
     */
    @Test(priority = 7)
    public void testPut4() {
        snapshotService.init();
        setIsOpenTransaction(true);
        ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>> txCache = getTxCache();
        txCache.put(SnapshotBizKeyEnum.CONTRACT, new ConcurrentHashMap<>());
        setTxCache(txCache);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "test");
        assertEquals(getTxCache().get(SnapshotBizKeyEnum.CONTRACT).get(JSON.toJSONString(new ContractSnapshotAgent.ContractCacheKey())), "test");
    }

    /**
     * txCache未包含key为key1的对象
     */
    @Test(priority = 7)
    public void testPut5() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "test");
        ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>> txCache = getTxCache();
        assertEquals(txCache.get(SnapshotBizKeyEnum.CONTRACT).get(JSON.toJSONString(new ContractSnapshotAgent.ContractCacheKey())), "test");
    }

    /**
     * packageCache包含key为key1的缓存对象innerMap，但innerMap不包含key为key2的对象（检查是否未commit 就写入了packageCache）
     */
    @Test(priority = 7)
    public void testPut6() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "test");
        ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> packageCache = getPackageCache();
        try {
            packageCache.get(SnapshotBizKeyEnum.CONTRACT).get(JSON.toJSONString(new ContractSnapshotAgent.ContractCacheKey()));
        } catch (Exception e) {
            assertTrue(e instanceof com.google.common.cache.CacheLoader.InvalidCacheLoadException);
        }

    }

    /**
     * packageCache包含key为key1的缓存对象innerMap，但innerMap包含key为key2的对象
     */
    @Test(priority = 7)
    public void testPut7() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "test");
        snapshotService.commit();
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "test1");
        try {
            ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> packageCache = getPackageCache();
            assertEquals(packageCache.get(SnapshotBizKeyEnum.CONTRACT).get(JSON.toJSONString(new ContractSnapshotAgent.ContractCacheKey())), "test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SnapshotBizKeyEnum key1为null
     */
    @Test(priority = 8)
    public void testGet() {
        snapshotService.init();
        try {
            snapshotService.get(null,new ContractSnapshotAgent.ContractCacheKey());
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot  null point  exception[SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION]");
        }
    }

    /**
     * Object key2为null
     */
    @Test(priority = 8)
    public void testGet1() {
        snapshotService.init();
        try {
            snapshotService.get(SnapshotBizKeyEnum.CONTRACT, null);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot  null point  exception[SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION]");
        }
    }

    /**
     * 两个入参都为null
     */
    @Test(priority = 8)
    public void testGet2() {
        snapshotService.init();
        try {
            snapshotService.get(null, null);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot  null point  exception[SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION]");
        }
    }

    /**
     * txCache不包含key为key1的缓存对象时
     */
    @Test(priority = 8)
    public void testGet3() {
        //TODO 暂时不测此case
    }

    /**
     * txCache包含key为key1的缓存对象txCacheInnerMap，但txCacheInnerMap不包含key为key2的对象
     */
    @Test(priority = 8)
    public void testGet4() {
        //TODO 暂时不测此case
        //assertNull(snapshotService.get(SnapshotBizKeyEnum.CONTRACT, "test1"));
    }

    /**
     * packageCache不包含key为key1的缓存对象时
     */
    @Test(priority = 8)
    public void testGet5() {
        snapshotService.init();
        try {
            ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> packageCache = getPackageCache();
            packageCache.remove(SnapshotBizKeyEnum.CONTRACT);
            setPackageCache(packageCache);
            snapshotService.get(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey());
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot core key not existed exception[SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION]");
        }
    }

    /**
     * packageCache包含key为key1的缓存对象innerMap，但innerMap不包含key为key2的对象
     */
    @Test(priority = 8)
    public void testGet6() {
        snapshotService.init();
        try {
            assertNull(snapshotService.get(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey()));
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot  query exception[SLAVE_SNAPSHOT_QUERY_EXCEPTION]");
        }
    }

    /**
     * 从txCache中成功获取到需要查询的对象
     */
    @Test(priority = 8)
    public void testGet7() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "test");
        assertEquals(snapshotService.get(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey()), "test");
    }

    /**
     * 从packageCache中成功获取到需要查询的对象
     */
    @Test(priority = 8)
    public void testGet8() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "test");
        snapshotService.commit();
        assertEquals(snapshotService.get(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey()), "test");
    }

    /**
     * 方法的返回值，是否是指定的clazz对象
     */
    @Test(priority = 8)
    public void testGet9() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "test");
        snapshotService.commit();
        Object obj = snapshotService.get(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey());
        assertTrue(obj instanceof String);
    }

    /**
     * isOpenTransaction为true的情形
     * 方法执行完成后，isOpenTransaction是否为false
     */
    @Test(priority = 9)
    public void testRollback() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.rollback();
        assertFalse(getIsOpenTransaction());
    }

    /**
     * isOpenTransaction为false的情形
     */
    @Test(priority = 9)
    public void testRollback1() {
        snapshotService.init();
        setIsOpenTransaction(false);
        try {
            snapshotService.rollback();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot transaction not started exception[SLAVE_SNAPSHOT_TRANSACTION_NOT_STARTED_EXCEPTION]");
        }
    }

    /**
     * 方法执行完成后，txCache是否为空
     */
    @Test(priority = 9)
    public void testRollback2() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.rollback();
        assertTrue(getTxCache().isEmpty());
    }

    /**
     * isOpenTransaction为true的情形
     * 方法执行完成后，isOpenTransaction是否为false
     */
    @Test(priority = 10)
    public void testCommit() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.commit();
        assertFalse(getIsOpenTransaction());
    }

    /**
     * isOpenTransaction为false的情形
     */
    @Test(priority = 10)
    public void testCommit1() {
        snapshotService.init();
        setIsOpenTransaction(false);
        try {
            snapshotService.commit();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot transaction not started exception[SLAVE_SNAPSHOT_TRANSACTION_NOT_STARTED_EXCEPTION]");
        }
    }

    /**
     * txCache为空的情形
     */
    @Test(priority = 10)
    public void testCommit2() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.commit();
        try {
            ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> packageCache = getPackageCache();
            assertNull(packageCache.get(SnapshotBizKeyEnum.CONTRACT).get(JSON.toJSONString(new ContractSnapshotAgent.ContractCacheKey())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * txCache不为空的情形
     * packageCache中的innerCache数据量未超过MAXIMUN_SIZE
     * txCache中的数据是否完整复制到packageCache
     */
    @Test(priority = 10)
    public void testCommit3() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "testCommit");
        snapshotService.commit();
        try {
            ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> packageCache = getPackageCache();
            assertEquals(packageCache.get(SnapshotBizKeyEnum.CONTRACT).get(JSON.toJSONString(new ContractSnapshotAgent.ContractCacheKey())), "testCommit");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法执行完成后，txCache是否为空
     */
    @Test(priority = 10)
    public void testCommit4() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "testCommit");
        snapshotService.commit();
        assertTrue(getTxCache().isEmpty());
    }

    /**
     * txCache与packageCache包含重复的key
     */
    @Test(priority = 10)
    public void testCommit5() {
        //TODO 待处理
//        snapshotService.init();
//        snapshotService.startTransaction();
//        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "testCommit");
//        snapshotService.commit();
//        Class<SnapshotServiceImpl> clazz = (Class<SnapshotServiceImpl>) snapshotService.getClass();
//        try {
//            Field field = clazz.getDeclaredField("txCache");
//            field.setAccessible(true);
//            ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>> txCache = (ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>>) field.get(snapshotService);
//            assertTrue(txCache.isEmpty());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * txCache包含空值的情形（不会存在）
     */
    @Test(priority = 10)
    public void testCommit6() {
        //TODO 待处理
    }

    /**
     * packageCache中的innerCache数据量超过MAXIMUN_SIZE
     */
    @Test(priority = 10)
    public void testCommit7() {
        snapshotService.init();
        setIsOpenTransaction(true);
        setMaxnumSize(0);
        try {
            snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "testCommit");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot cache size not enough exception[SLAVE_SNAPSHOT_CACHE_SIZE_NOT_ENOUGH_EXCEPTION]");
        }
    }

    /**
     * packageCache中是否含有key为SnapshotBizKeyEnum.UTXO类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.MERKLE_TREE类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.MANAGE类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.DATA_IDENTITY类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.CONTRACT类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.FREEZE类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.CONTRACT类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.CONTRACT_CONTRACT_BIND类型缓存对象
     * packageCache中是否含有key为SnapshotBizKeyEnum.CONTRACT_SATE类型缓存对象
     * @param params
     */
    @Test(dataProvider = "destroyData", priority = 11)
    public void testDestroy(Map<?,?> params) {
        setIsOpenTransaction(false);
        snapshotService.destroy();
        String key = params.get("key").toString();
        SnapshotBizKeyEnum keyEnum = SnapshotBizKeyEnum.valueOf(key);
        assertTrue(getPackageCache().containsKey(keyEnum));
    }

    /**
     * 方法执行完成后，txCache是否为空（map为空）
     */
    @Test(priority = 11)
    public void testDestroy1() {
        snapshotService.init();
        snapshotService.destroy();
        assertTrue(getTxCache().isEmpty());
    }

    /**
     * 方法执行完成后，isOpenTransaction是否为false
     */
    @Test(priority = 11)
    public void testDestroy2() {
        snapshotService.init();
        snapshotService.destroy();
        assertFalse(getIsOpenTransaction());
    }

    /**
     * packageCache为空时，执行该方法应该会抛出异常
     */
    @Test(priority = 11)
    public void testDestroy3() {
        snapshotService.init();
        setPackageCache(new ConcurrentHashMap<>());
        try {
            snapshotService.destroy();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "snapshot  not init exception[SLAVE_SNAPSHOT_NOT_INIT_EXCEPTION]");
        }
    }

    /**
     * 插入两个相同key数据，value不同看取出数据，看数据是否被修改
     */
    @Test(priority = 12)
    public void test1() {
        snapshotService.init();
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "testCommit");
        snapshotService.commit();
        String testCommit = (String) snapshotService.get(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey());
        setIsOpenTransaction(true);
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "testCommit1");
        snapshotService.commit();
        String testCommit1 = (String) snapshotService.get(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey());
        assertEquals(testCommit, "testCommit");
        assertEquals(testCommit1, "testCommit1");
        assertFalse(testCommit.equals(testCommit1));
    }

    /**
     * 写入cache的数据,外部修改，再get检查是否cache 也被修改了
     */
    @Test(priority = 12)
    public void test2() {
        snapshotService.init();
        setIsOpenTransaction(true);
        ContractPO contractPO = new ContractPO();
        contractPO.setLanguage("java");
        snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), contractPO);
        snapshotService.commit();
        contractPO.setLanguage("JS");
        ContractPO po = (ContractPO) snapshotService.get(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey());
        assertEquals(po.getLanguage(), "java");
    }


    /**
     * ==========================================================================================
     */

    private ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> getPackageCache() {
        Class<SnapshotServiceImpl> clazz = (Class<SnapshotServiceImpl>) snapshotService.getClass();
        try {
            Field field = clazz.getDeclaredField("packageCache");
            field.setAccessible(true);
            ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> packageCache = (ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>>) field.get(snapshotService);
            return packageCache;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>> getTxCache() {
        Class<SnapshotServiceImpl> clazz = (Class<SnapshotServiceImpl>) snapshotService.getClass();
        try {
            Field fieldTxCache = clazz.getDeclaredField("txCache");
            fieldTxCache.setAccessible(true);
            ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>> txCache = (ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>>) fieldTxCache.get(snapshotService);
            return txCache;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean getIsOpenTransaction() {
        Class<SnapshotServiceImpl> clazz = (Class<SnapshotServiceImpl>) snapshotService.getClass();
        try {
            Field field = clazz.getDeclaredField("isOpenTransaction");
            field.setAccessible(true);
            boolean isOpenTransaction = field.getBoolean(snapshotService);
            return isOpenTransaction;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setIsOpenTransaction(boolean flag) {
        Class<SnapshotServiceImpl> clazz = (Class<SnapshotServiceImpl>) snapshotService.getClass();
        try {
            Field field = clazz.getDeclaredField("isOpenTransaction");
            field.setAccessible(true);
            field.set(snapshotService, flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMaxnumSize(int maxSize) {
        Class<SnapshotServiceImpl> clazz = (Class<SnapshotServiceImpl>) snapshotService.getClass();
        try {
            Field field = clazz.getDeclaredField("MAXIMUN_SIZE");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setAccessible(true);
            field.setInt(snapshotService, maxSize);
            snapshotService.put(SnapshotBizKeyEnum.CONTRACT, new ContractSnapshotAgent.ContractCacheKey(), "testCommit");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTxCache(ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>> cache) {
        Class<SnapshotServiceImpl> clazz = (Class<SnapshotServiceImpl>) snapshotService.getClass();
        try {
            Field fieldTxCache = clazz.getDeclaredField("txCache");
            fieldTxCache.setAccessible(true);
            fieldTxCache.set(snapshotService, cache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPackageCache(ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> cache) {
        Class<SnapshotServiceImpl> clazz = (Class<SnapshotServiceImpl>) snapshotService.getClass();
        try {
            Field field = clazz.getDeclaredField("packageCache");
            field.setAccessible(true);
            field.set(snapshotService, cache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}