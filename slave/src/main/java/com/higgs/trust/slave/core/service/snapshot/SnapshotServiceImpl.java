package com.higgs.trust.slave.core.service.snapshot;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.JsonUtils;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.api.enums.SnapshotValueStatusEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.exception.SnapshotException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.service.snapshot.agent.*;
import com.higgs.trust.slave.model.bo.snapshot.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * memory SnapshotServiceImpl
 *
 * @author lingchao
 * @create 2018年04月09日22:09
 */
@Slf4j
@Service
public class SnapshotServiceImpl implements SnapshotService, InitializingBean {
    /**
     * tag whether  the snapshot is in  transaction
     */
    private static boolean isOpenTransaction = false;

    private static int index = 0;

    private static final int PACKAGE_MAXIMUN_SIZE = 10000;

    private static final int GLOBAL_MAXIMUN_SIZE = 10000;

    private static final int REFRESH_TIME = 365;

    private Lock lock = new ReentrantLock();

    @Autowired
    private UTXOSnapshotAgent utxoSnapshotAgent;

    @Autowired
    private MerkleTreeSnapshotAgent merkleTreeSnapshotAgent;

    @Autowired
    private ManageSnapshotAgent manageSnapshotAgent;

    @Autowired
    private DataIdentitySnapshotAgent dataIdentitySnapshotAgent;

    @Autowired
    private AccountSnapshotAgent accountSnapshotAgent;

    @Autowired
    private AccountDetailSnapshotAgent accountDetailSnapshotAgent;

    @Autowired
    private FreezeSnapshotAgent freezeSnapshotAgent;

    @Autowired
    private ContractSnapshotAgent contractSnapshotAgent;

    @Autowired
    private AccountContractBindingSnapshotAgent accountContractBindingSnapshotAgent;

    @Autowired
    private ContractStateSnapshotAgent contractStateSnapshotAgent;

    @Autowired
    private CaSnapshotAgent caSnapshotAgent;


    /**
     * cache  for snapshot cacheLoader
     */
    private ConcurrentHashMap<SnapshotBizKeyEnum, CacheLoader> cacheLoaderCache = new ConcurrentHashMap<>();

    /**
     * cache  for global
     */
    private ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> globalCache = new ConcurrentHashMap<>();

    /**
     * cache  for package
     */
    private ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Value>> packageCache = new ConcurrentHashMap<>();
    /**
     * cache for transaction
     */
    private ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Value>> txCache = new ConcurrentHashMap<>();

    /**
     * register all the caches to packageSnapshot,only run when the application is starting.
     */
    private void init() {
        log.debug("Start to register cache loader, get lock for it");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.debug("Get lock failed, stop to init snapshot!");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_GET_NO_LOCK_EXCEPTION);
        }
        try {
            log.debug("Get lock success, go init cache!");
            log.debug(("Clear txCache and packageCache and globalCache  and cacheLoaderCache  first"));
            txCache.clear();
            packageCache.clear();
            globalCache.clear();
            cacheLoaderCache.clear();
            //register UTXO cache loader
            log.debug("Register UTXO cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.UTXO, utxoSnapshotAgent);

            //register MERKLE_TREE cache loader
            log.debug("Register MERKLE_TREE cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.MERKLE_TREE, merkleTreeSnapshotAgent);

            //register MANAGE cache loader
            log.debug("Register MANAGE cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.MANAGE, manageSnapshotAgent);

            //register DATA_IDENTITY cache loader
            log.debug("Register DATA_IDENTITY cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.DATA_IDENTITY, dataIdentitySnapshotAgent);

            //register ACCOUNT cache loader
            log.debug("Register ACCOUNT cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.ACCOUNT, accountSnapshotAgent);

            log.debug("Register ACCOUNT_DETAIL cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.ACCOUNT_DETAIL, accountDetailSnapshotAgent);

            //register ACCOUNT cache loader
            log.debug("Register FREEZE cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.FREEZE, freezeSnapshotAgent);

            //register CONTRACT cache loader
            log.debug("Register CONTRACT cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.CONTRACT, contractSnapshotAgent);

            //register ACCOUNT_CONTRACT_BIND cache loader
            log.debug("Register ACCOUNT_CONTRACT_BIND cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.ACCOUNT_CONTRACT_BIND, accountContractBindingSnapshotAgent);

            //register CONTRACT STATE cache loader
            log.debug("Register CONTRACT STATE cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.CONTRACT_SATE, contractStateSnapshotAgent);

            //registerCA cache loader
            log.debug("Register CA cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.CA, caSnapshotAgent);
        } finally {
            log.debug("unlock lock  for init snapshot");
            lock.unlock();
        }

        log.debug("End of register cache loader");
    }

    /**
     * start the snapshot transaction.Tag isOpenTransaction to be true.
     */
    @Override
    public void startTransaction() {
        Profiler.enter("[Snapshot.startTransaction]");
        log.debug("Start to start snapshot transaction, and get lock for it");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.debug("Get lock failed, stop to startTransaction!");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_GET_NO_LOCK_EXCEPTION);
        }
        try {
            log.debug("Get lock success, go to start transaction!");
            //check whether snapshot transaction has been started.
            isOpenTransactionException();

            //clear txCache
            log.debug("Clear txCache");
            txCache.clear();

            //sign it as in transaction
            isOpenTransaction = true;
        } finally {
            log.debug("unlock lock for startTransaction");
            lock.unlock();
        }
        log.debug("End of start snapshot transaction");
        Profiler.release();
    }

    /**
     * clear packageCache and txCache
     */
    @Override
    public void clear() {
        Profiler.enter("[Snapshot.destroy]");
        log.debug("Start to destroy snapshot");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.debug("Get lock failed, stop to clear snapshot!");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_GET_NO_LOCK_EXCEPTION);
        }
        try {
            log.debug("Get lock success, go to clear snapshot!");
            //close transaction first,if not there may be some data put into cache after clearing data
            closeTransaction();

            //clear packageCache and txCache
            clearTempCache();

            //reset index
            resetIndex();
        } finally {
            log.debug("Unlock lock for clear snapshot!");
            lock.unlock();
        }
        log.debug("End of destroy snapshot");
        Profiler.release();
    }


    /**
     * clear globalCache and packageCache and txCache
     */
    @Override
    public void destroy() {
        Profiler.enter("[Snapshot.destroy]");
        log.debug("Start to destroy snapshot");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.debug("Get lock failed, stop to destroy snapshot!");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_GET_NO_LOCK_EXCEPTION);
        }
        try {
            log.debug("Get lock success, go to destroy snapshot!");
            //close transaction first,if not, there may be some data put into cache after clearing data
            closeTransaction();

            //clear packageCache and txCache
            clearTempCache();

            //check whether there is data in the globalCache
            log.debug("Clear globalCache");
            if (globalCache.isEmpty()) {
                log.error("The snapshot has not init");
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_NOT_INIT_EXCEPTION);
            }

            //clear packageCache inner cache
            for (Map.Entry<SnapshotBizKeyEnum, LoadingCache<String, Object>> outerEntry : globalCache.entrySet()) {
                log.debug("Clear snapshotBizKeyEnum: {}  from  globalCache", outerEntry.getKey());
                LoadingCache<String, Object> innerMap = outerEntry.getValue();
                innerMap.invalidateAll();
            }

            //reset index
            resetIndex();
        } finally {
            log.debug("Unlock lock for destroy snapshot!");
            lock.unlock();
        }
        log.debug("End of destroy snapshot");
        Profiler.release();
    }


    /**
     * 1.get object from snapshot cache .It will get it from txCache first.
     * 2.If there is not object ,then we will get it from packageCache.
     * 3.If there is not object ,then we will get it from globalCache.
     * 4.If there is not object ,then we will get it from db.
     *
     * @param key1
     * @param key2
     * @return
     */
    @Override
    public Object get(SnapshotBizKeyEnum key1, Object key2) {
        Profiler.enter("[Snapshot.get]");
        try {
            log.debug("Start to get data for snapshotBizKeyEnum:{}, bizKey:{}", key1, key2);
            //Check null
            if (null == key1 || null == key2) {
                log.error("Get data from snapshot ,the key1  and key2 can not be null, in fact key1 = {}, key2 = {}", key1, key2);
                throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION, "Put data into snapshot key1 and key2 can not be null!");
            }

            //get data from txCache
            Value value = getDataFromTxCache(key1, key2);
            if (null != value) {
                log.debug("Get snapshotBizKeyEnum: {} , innerKey : {} , value :{} from  snapshot, it is in the txCache", key1, key2, value.getObject());
                return transferValue(value.getObject());
            }

            //get data from packageCache
            value = getDataFromPackageCache(key1, key2);
            if (null != value) {
                log.debug("Get snapshotBizKeyEnum: {} , innerKey : {} , value :{} from  snapshot, it is in the packageCache", key1, key2, value.getObject());
                return transferValue(value.getObject());
            }

            //get data from globalCache
            Object object = getDataFromGlobalCache(key1, key2);

            log.debug("End of get data for snapshotBizKeyEnum:{}, bizKey:{}", key1, key2);
            if (null == object) {
                return object;
            }

            return transferValue(object);
        } finally {
            Profiler.release();
        }
    }

    /**
     * insert  object into the snapshot  txCache
     *
     * @param key1
     * @param key2
     * @param value
     */
    @Override
    public void insert(SnapshotBizKeyEnum key1, Object key2, Object value) {
        Profiler.enter("[Snapshot.insert]");
        try {
            log.debug("Start to insert data {}  for snapshotBizKeyEnum:{}, bizKey:{}", value, key1, key2);

            //check  before insert and update
            putCheck(key1, key2, value);

            log.debug("Insert snapshotBizKeyEnum: {} , innerKey : {} , value :{} into txCache", key1, key2, value);

            String innerKey = serializeJsonString(key2);
            ConcurrentHashMap<String, Value> innerMap = txCache.get(key1);

            //whether the data  existed in txCache
            boolean isExistedInTxCache = null != innerMap.get(innerKey);
            //whether the data existed in packageCache
            boolean isExistedInPackageCache = null != getDataFromPackageCache(key1, key2);

            if (isExistedInTxCache || isExistedInPackageCache) {
                log.error("Insert snapshotBizKeyEnum: {} , innerKey : {} , value :{} into txCache duplicate key exception", key1, key2, value);
                MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_SNAPSHOT_DUPLICATE_KEY_EXCEPTION.getMonitorTarget(), 1);
                throw new DuplicateKeyException("Insert data into txCache duplicate key exception");
            }

            //put insert status data into txCache
            Value putValue = buildValue(value, SnapshotValueStatusEnum.INSERT.getCode(), index);
            innerMap.put(innerKey, (Value) transferValue(putValue));

            //add index for next data
            addIndex();

            //check whether snapshot transaction has been started.
            isNotOpenTransactionException();
            log.debug("End of insert data {} into txCache for snapshotBizKeyEnum:{}, bizKey:{}", value, key1, key2);
        } finally {
            Profiler.release();
        }
    }

    /**
     * update  object into the snapshot  txCache
     *
     * @param key1
     * @param key2
     * @param value
     */
    @Override
    public void update(SnapshotBizKeyEnum key1, Object key2, Object value) {
        Profiler.enter("[Snapshot.update]");
        try {
            if (log.isDebugEnabled()) {
                log.debug("Start to update data {}  for snapshotBizKeyEnum:{}, bizKey:{}", value, key1, key2);
            }

            //check  before insert and update
            putCheck(key1, key2, value);

            if (log.isDebugEnabled()) {
                log.debug("Update snapshotBizKeyEnum: {} , innerKey : {} , value :{} into txCache", key1, key2, value);
            }
            // put update Data into txCache
            putUpdateDataIntoTxCache(key1, key2, value);

            //add index
            addIndex();

            //check whether snapshot transaction has been started.
            isNotOpenTransactionException();
            if (log.isDebugEnabled()) {
                log.debug("End of update data {}  into txCache for snapshotBizKeyEnum:{}, bizKey:{}", value, key1, key2);
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * put update Data into txCache
     *
     * @param key1
     * @param key2
     * @param value
     */
    private void putUpdateDataIntoTxCache(SnapshotBizKeyEnum key1, Object key2, Object value) {

        String innerKey = serializeJsonString(key2);
        ConcurrentHashMap<String, Value> innerMap = txCache.get(key1);
        Value putInsertValue = buildValue(value, SnapshotValueStatusEnum.INSERT.getCode(), index);
        Value putUpdateValue = buildValue(value, SnapshotValueStatusEnum.UPDATE.getCode(), index);

        Value txValue = innerMap.get(innerKey);
        if (null != txValue) {
            //data in txCache
            if (StringUtils.equals(txValue.getStatus(), SnapshotValueStatusEnum.INSERT.getCode())) {
                //use the index before in the cache
                putInsertValue.setIndex(txValue.getIndex());
                innerMap.put(innerKey, (Value) transferValue(putInsertValue));
            } else {
                innerMap.put(innerKey, (Value) transferValue(putUpdateValue));
            }
        } else {
            //data not in txCache
            Value packageValue = getDataFromPackageCache(key1, key2);
            if (null != packageValue) {
                innerMap.put(innerKey, (Value) transferValue(putUpdateValue));
            } else {
                checkGlobalAndPut(key1, key2, innerKey, innerMap, putUpdateValue);
            }
        }
    }

    /**
     * check whether data in global cache ,if not throw exception,else put update data into txCache
     *
     * @param key1
     * @param key2
     * @param innerKey
     * @param innerMap
     * @param putUpdateValue
     */
    private void checkGlobalAndPut(SnapshotBizKeyEnum key1, Object key2, String innerKey, ConcurrentHashMap<String, Value> innerMap, Value putUpdateValue) {
        Object globalValue = getDataFromGlobalCache(key1, key2);
        if (null != globalValue) {
            innerMap.put(innerKey, (Value) transferValue(putUpdateValue));
        } else {
            log.error("Update snapshotBizKeyEnum: {} , innerKey : {} into packageCache exception, the data to be updated is not exist in packageCache  and and globalCache and DB and txCache", key1, innerKey);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_SNAPSHOT_DATA_NOT_EXIST_EXCEPTION.getMonitorTarget(), 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_DATA_NOT_EXIST_EXCEPTION, "Update data into packageCache  exception, the data to be updated is not exist in packageCache  and and globalCache and DB and txCache");
        }
    }
    /**
     * 1. copy the txCache to packageCache
     * 2.clear txCache
     * 3.tag the isOpenTransaction to be false
     */
    @Override
    public void commit() {
        Profiler.enter("[Snapshot.commit]");
        log.debug("Start to commit");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.debug("Get lock failed, stop to commit!");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_GET_NO_LOCK_EXCEPTION);
        }
        try {
            log.debug("Get lock success, go to commit!");
            //check whether snapshot transaction has been started.
            isNotOpenTransactionException();

            //close transaction first,if not there may be some data put into cache after clearing data
            closeTransaction();

            //check whether snapshot txCache is empty.
            if (txCache.isEmpty()) {
                return;
            }

            //copy data from txCache to packageCache
            copyDataToPackageCache();

            //clear txCache
            txCache.clear();
        } finally {
            log.debug("Unlock lock for commit!");
            lock.unlock();
            Profiler.release();
        }
        log.debug("End of commit ");
    }

    /**
     * 1.clear txCache
     * 2.tag the isOpenTransaction to be false
     */
    @Override
    public void rollback() {
        Profiler.enter("[Snapshot.rollback]");
        log.debug("Start to rollback");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.debug("Get lock failed, stop to commit!");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_GET_NO_LOCK_EXCEPTION);
        }
        try {
            log.debug("Get lock success, go to rollback!");
            //check whether snapshot transaction has been started.
            isNotOpenTransactionException();

            //close transaction,if not there may be some data put into cache after clearing data
            closeTransaction();

            //clear txCache
            txCache.clear();
        } finally {
            log.debug("Unlock lock for rollback!");
            lock.unlock();
            Profiler.release();
        }
        log.debug("End of rollback");
    }

    /**
     * 1.flush data into globalCache
     * 2.flush data into db
     */
    @Override
    public void flush() {
        Profiler.enter("[Snapshot.flush]");
        log.debug("Start to flush");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.debug("Get lock failed, stop to flush!");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_GET_NO_LOCK_EXCEPTION);
        }
        try {
            log.debug("Get lock success, go to flush!");
            //check whether snapshot transaction has been started.
            isOpenTransactionException();

            //check whether snapshot packageCache is empty.
            if (packageCache.isEmpty()) {
                return;
            }

            //flush data into globalCache
            copyDataToGlobalCache();

            //flush data into BD
            flushDataIntoDB();

            //clear packageCache and txCache
            clearTempCache();

            //clear merkle tree cache
            clearGlobalMerkleTree();

            //reset index
            resetIndex();
        } finally {
            log.debug("Unlock lock for flush!");
            lock.unlock();
            Profiler.release();
        }
        log.debug("End of flush");
    }

    /**
     * clear global merkle cache
     */
    private void clearGlobalMerkleTree() {
        SnapshotBizKeyEnum bizKey = SnapshotBizKeyEnum.MERKLE_TREE;
        //check whether there is snapshotBizKeyEnum in globalCache
        if (!globalCache.containsKey(bizKey)) {
            log.error("There is no key:{} for bizCache in globalCache!", bizKey);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXIST_EXCEPTION.getMonitorTarget(), 1);
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION);
        }
        LoadingCache<String, Object> merkleCache = globalCache.get(bizKey);
        merkleCache.invalidateAll();
    }

    /**
     * 1.register  loading cache method  to guavaCache
     * 2.add guavaCache to globalCache
     * 3.add cacheLoader to cacheLoaderCache
     */
    private void registerBizLoadingCache(SnapshotBizKeyEnum snapshotBizKeyEnum, CacheLoader cacheLoader) {
        log.debug("Start to register core loadingCache to globalCache and cacheLoaderCache for snapshotBizKeyEnum:{}", snapshotBizKeyEnum);
        LoadingCache<String, Object> bizCache = CacheBuilder.newBuilder().initialCapacity(10).maximumSize(GLOBAL_MAXIMUN_SIZE).refreshAfterWrite(REFRESH_TIME, TimeUnit.DAYS).build(new com.google.common.cache.CacheLoader<String, Object>() {
            @Override
            public Object load(String bo) throws Exception {
                log.debug("There is no data for  bizKey： {}  by snapshotBizKeyEnum： {} in globalCache ,try to get data from DB", bo, snapshotBizKeyEnum);
                //just want to get Clazz
                Object object = JSON.parse(bo);
                return cacheLoader.query(object);
            }
        });

        //add guavaCache to globalCache
        globalCache.put(snapshotBizKeyEnum, bizCache);

        // add cacheLoader to cacheLoaderCache
        cacheLoaderCache.put(snapshotBizKeyEnum, cacheLoader);
        log.debug("End of register core loadingCache to globalCache and cacheLoaderCache for snapshotBizKeyEnum:{}", snapshotBizKeyEnum);
    }

    /**
     * close transaction
     */
    private void closeTransaction() {
        log.debug("Close the snapshot transaction");
        isOpenTransaction = false;
    }


    /**
     * get data from txCache
     *
     * @param key1
     * @param key2
     * @return
     */
    private Value getDataFromTxCache(SnapshotBizKeyEnum key1, Object key2) {
        String innerKey = serializeJsonString(key2);
        //check where there is key1 in txCache
        if (!txCache.containsKey(key1)) {
            log.debug("There is no  snapshotBizKeyEnum: {} in the txCache ", key1);
            return null;
        }
        //get data from txCacheInnerMap
        ConcurrentHashMap<String, Value> txCacheInnerMap = txCache.get(key1);
        return txCacheInnerMap.get(innerKey);
    }

    /**
     * get data from packageCache
     *
     * @param key1
     * @param key2
     * @return
     */
    private Value getDataFromPackageCache(SnapshotBizKeyEnum key1, Object key2) {

        String innerKey = serializeJsonString(key2);
        //check where there is key1 in packageCache
        if (!packageCache.containsKey(key1)) {
            log.debug("There is no  snapshotBizKeyEnum: {} in the packageCache ", key1);
            return null;
        }
        //get data from packageCacheInnerMap
        ConcurrentHashMap<String, Value> packageCacheInnerMap = packageCache.get(key1);
        return packageCacheInnerMap.get(innerKey);
    }


    /**
     * get data from globalCache
     *
     * @param key1
     * @param key2
     * @return
     */
    private Object getDataFromGlobalCache(SnapshotBizKeyEnum key1, Object key2) {

        //check whether there is snapshotBizKeyEnum in globalCache
        if (!globalCache.containsKey(key1)) {
            log.error("There is no key:{} for bizCache in globalCache!", key1);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXIST_EXCEPTION.getMonitorTarget(), 1);
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION);
        }
        //1.Get data from globalCache.It will return data when there is data in the globalCache
        //2.else it will get data from db .
        //3.If there is no data in db ,it will return null
        //4.else return data and put data into globalCache
        Object value = null;
        String innerKey = serializeJsonString(key2);
        LoadingCache<String, Object> innerMap = globalCache.get(key1);
        try {
            value = innerMap.get(innerKey);
            log.debug("Get snapshotBizKeyEnum: {} , innerKey : {} , value :{} from  snapshot, it is from  the globalCache", key1, innerKey, value);
        } catch (Throwable e) {
            if (!(e instanceof com.google.common.cache.CacheLoader.InvalidCacheLoadException)) {
                log.error("There is  exception happened to query data for  snapshotBizKeyEnum: {} , innerKey : {}  in  snapshot and db", key1, innerKey, e);
                MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_SNAPSHOT_QUERY_EXCEPTION.getMonitorTarget(), 1);
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_QUERY_EXCEPTION, e);
            }
            log.debug("There is no data for  snapshotBizKeyEnum: {} , innerKey : {}  in  snapshot and db", key1, innerKey);
        }
        return value;
    }


    /**
     * copy data from txCache to packageCache
     */
    private void copyDataToPackageCache() {
        log.debug("Start to copy data from txCache to packageCache");
        for (Map.Entry<SnapshotBizKeyEnum, ConcurrentHashMap<String, Value>> outerEntry : txCache.entrySet()) {

            SnapshotBizKeyEnum snapshotBizKeyEnum = outerEntry.getKey();
            ConcurrentHashMap<String, Value> innerMap = outerEntry.getValue();
            //check whether there is snapshotBizKeyEnum in packageCache
            if (!packageCache.containsKey(snapshotBizKeyEnum)) {
                log.debug("There is no key:{} in packageCache!", snapshotBizKeyEnum);
                packageCache.put(snapshotBizKeyEnum, new ConcurrentHashMap<>());
            }

            //check whether inner map is empty.
            if (innerMap.isEmpty()) {
                log.debug("The inner map for Snapshot txCache key :  {}  is empty. jump to next txCache key", snapshotBizKeyEnum);
                continue;
            }

            // foreach inner map to copy data
            ConcurrentHashMap<String, Value> innerCache = packageCache.get(snapshotBizKeyEnum);
            for (Map.Entry<String, Value> innerEntry : innerMap.entrySet()) {
                //check  cache size
                if (innerCache.size() >= PACKAGE_MAXIMUN_SIZE) {
                    log.error("Cache size  : {} for key:{} in packageCache is equal or bigger than {}!", innerCache.size(), snapshotBizKeyEnum, PACKAGE_MAXIMUN_SIZE);
                    MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_SNAPSHOT_PACKAGE_OVERSIZE_EXCEPTION.getMonitorTarget(), 1);
                    throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_CACHE_SIZE_NOT_ENOUGH_EXCEPTION);
                }

                //put data into package inner cache
                putDataIntoPackageCache(innerCache, innerEntry, snapshotBizKeyEnum);
            }
        }
        log.debug("End  of copy data from txCache to packageCache");
    }


    /**
     * put data into package inner cache
     *
     * @param packageInnerCache
     * @param txCacheInnerEntry
     * @param snapshotBizKeyEnum
     */
    private void putDataIntoPackageCache(ConcurrentHashMap<String, Value> packageInnerCache, Map.Entry<String, Value> txCacheInnerEntry, SnapshotBizKeyEnum snapshotBizKeyEnum) {

        String innerKey = txCacheInnerEntry.getKey();
        Value innerValue = txCacheInnerEntry.getValue();

        Object keyObject = JSON.parse(innerKey);
        //txCache value is insert status
        boolean isInsert = StringUtils.equals(innerValue.getStatus(), SnapshotValueStatusEnum.INSERT.getCode());
        //data in PackageCache
        Value packageValue = getDataFromPackageCache(snapshotBizKeyEnum, keyObject);
        boolean isExistedInPackageCache = null != packageValue;

        log.debug("Put SnapshotBizKeyEnum: {} , innerKey : {} , value :{} into packageCache", snapshotBizKeyEnum, innerKey, innerValue);


        //if there is no data in package cache. put data into packageCache
        if (!isExistedInPackageCache) {
            packageInnerCache.put(innerKey, innerValue);
            return;
        }

        //if the txCache value is insert status and there is data in package cache, it is a exception
        if (isInsert && isExistedInPackageCache) {
            log.error("Insert snapshotBizKeyEnum: {} , innerKey : {} into packageCache duplicate key exception", snapshotBizKeyEnum, innerKey);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_SNAPSHOT_DUPLICATE_KEY_EXCEPTION.getMonitorTarget(), 1);
            throw new DuplicateKeyException("Insert data into packageCache duplicate key exception");
        }

        //if the txCache value is update status and there is  data in package cache  and the status is insert, put data into packageCache
        if (!isInsert && isExistedInPackageCache && StringUtils.equals(packageValue.getStatus(), SnapshotValueStatusEnum.INSERT.getCode())) {
            packageInnerCache.put(innerKey, buildValue(innerValue.getObject(), SnapshotValueStatusEnum.INSERT.getCode(), packageValue.getIndex()));
            return;
        }

        //if the txCache value is update status and there is  data in package cache  and the status is update, put data into packageCache
        if (!isInsert && isExistedInPackageCache && !StringUtils.equals(packageValue.getStatus(), SnapshotValueStatusEnum.INSERT.getCode())) {
            packageInnerCache.put(innerKey, innerValue);
            return;
        }

    }

    /**
     * copy data from packageCache to globalCache
     */
    private void copyDataToGlobalCache() {
        log.debug("Start to copy data from packageCache to globalCache");
        for (Map.Entry<SnapshotBizKeyEnum, ConcurrentHashMap<String, Value>> outerEntry : packageCache.entrySet()) {

            SnapshotBizKeyEnum snapshotBizKeyEnum = outerEntry.getKey();
            ConcurrentHashMap<String, Value> innerMap = outerEntry.getValue();
            //check whether there is snapshotBizKeyEnum in globalCache
            if (!globalCache.containsKey(snapshotBizKeyEnum)) {
                log.error("There is no key:{} in globalCache!", snapshotBizKeyEnum);
                MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXIST_EXCEPTION.getMonitorTarget(), 1);
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION);
            }

            //check whether inner map is empty.
            if (outerEntry.getValue().isEmpty()) {
                log.debug("The inner map for Snapshot core key :  {}  is empty. jump to next core key", snapshotBizKeyEnum);
                continue;
            }

            // foreach inner map to copy data
            LoadingCache<String, Object> innerCache = globalCache.get(snapshotBizKeyEnum);
            for (Map.Entry<String, Value> innerEntry : innerMap.entrySet()) {
                Value value = innerEntry.getValue();
                /**
                 * TODO do not check data is in db for performance
                 **/
                if (log.isDebugEnabled()) {
                    log.debug("Put SnapshotBizKeyEnum: {} , innerKey : {} , value :{} into globalCache",
                        snapshotBizKeyEnum, innerEntry.getKey(), innerEntry.getValue());
                }
                //put data into global cache
                innerCache.put(innerEntry.getKey(), value.getObject());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("End  of copy data from txCache to globalCache");
        }
    }

    /**
     * flush data into db
     */
    private void flushDataIntoDB() {
        log.debug("Start to flush data into db");
        for (Map.Entry<SnapshotBizKeyEnum, ConcurrentHashMap<String, Value>> outerEntry : packageCache.entrySet()) {
            //key and value
            SnapshotBizKeyEnum snapshotBizKeyEnum = outerEntry.getKey();
            ConcurrentHashMap<String, Value> innerMap = outerEntry.getValue();

            //check whether inner map is empty.
            if (innerMap.isEmpty()) {
                log.debug("The inner map for Snapshot core key :  {}  is empty. jump to next core key", snapshotBizKeyEnum);
                continue;
            }

            //check whether there is snapshotBizKeyEnum in cacheLoaderCache
            if (!cacheLoaderCache.containsKey(snapshotBizKeyEnum)) {
                log.error("There is no key:{} for cacheLoader in cacheLoaderCache!", snapshotBizKeyEnum);
                MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXIST_EXCEPTION.getMonitorTarget(), 1);
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION);
            }

            //transfer map ro sorted list
            List<Pair<Object, Object>> sortingList = mapToSortedList(innerMap);

            //cacheLoader to  flush data to db
            CacheLoader cacheLoader = cacheLoaderCache.get(snapshotBizKeyEnum);
            //all insert data for the biz
            List<Pair<Object, Object>> insertList = new ArrayList<>();
            //all update data for the biz
            List<Pair<Object, Object>> updateList = new ArrayList<>();
            // foreach inner map to flush data
            for (Pair<Object, Object> pair : sortingList) {
                Object keyObject = pair.getLeft();
                Value value = (Value) pair.getRight();
                //if data status is insert put it into  insertMap  else into updateMap
                if (StringUtils.equals(value.getStatus(), SnapshotValueStatusEnum.INSERT.getCode())) {
                    insertList.add(Pair.of(keyObject, value.getObject()));
                } else {
                    updateList.add(Pair.of(keyObject, value.getObject()));
                }
                log.debug("Put SnapshotBizKeyEnum: {} , innerKey : {} , value :{} into flush map ", snapshotBizKeyEnum, keyObject, value);
            }

            try {

                //flush insert data into db
                if (CollectionUtils.isNotEmpty(insertList) && !cacheLoader.batchInsert(insertList)) {
                    log.error("Flush data for batchInsert db failed error");
                    throw new SnapshotException(SlaveErrorEnum.SLAVE_DATA_NOT_INSERT_EXCEPTION);
                }

                //flush update data into db
                if (CollectionUtils.isNotEmpty(updateList) && !cacheLoader.batchUpdate(updateList)) {
                    log.error("Flush data for batchUpdate db failed error");
                     throw new SnapshotException(SlaveErrorEnum.SLAVE_DATA_NOT_UPDATE_EXCEPTION);
                }
            } catch (Throwable e) {
                log.error("Flush db exception", e);
                MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_SNAPSHOT_FLUSH_EXCEPTION.getMonitorTarget(), 1);
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_FLUSH_DATA_EXCEPTION);
            }
        }
        log.debug("End of flushing data into db");
    }

    /**
     * transfer map to sorted list
     *
     * @param innerMap
     * @return
     */
    private List<Pair<Object, Object>> mapToSortedList(ConcurrentHashMap<String, Value> innerMap) {
        List<Pair<Object, Object>> sortingList = new ArrayList<>();
        // foreach inner map to make list to sort data
        for (Map.Entry<String, Value> innerEntry : innerMap.entrySet()) {
            Object keyObject = JSON.parse(innerEntry.getKey());
            Value value = innerEntry.getValue();
            sortingList.add(Pair.of(keyObject, value));
        }
        //sort list
        sortValueListByIndex(sortingList);
        return sortingList;
    }

    /**
     * sort list by value index
     *
     * @param sortingList
     */
    //TODO lingchao 优化代码。用Lambda  https://www.cnblogs.com/tomyLi/p/JAVA8rang-dai-ma-geng-you-ya-zhiList-pai-xu.html
    private void sortValueListByIndex(List<Pair<Object, Object>> sortingList) {
        //sort list
        Collections.sort(sortingList, new Comparator<Pair<Object, Object>>() {
            @Override
            public int compare(Pair<Object, Object> p1, Pair<Object, Object> p2) {
                Value value1 = (Value) p1.getRight();
                Value value2 = (Value) p2.getRight();
                if (value1.getIndex() >= value2.getIndex()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }


    /**
     * transfer the data to a different object
     *
     * @param object
     * @return
     */
    private Object transferValue(Object object) {
        //TODO find a way make the object const
        String valueTemp = serializeJsonString(object);
        return JSON.parseObject(valueTemp, object.getClass());
    }

    /**
     * clear packageCache and txCache
     */
    private void clearTempCache() {
        //clear txCache
        log.debug("Clear txCache");
        txCache.clear();

        //clear packageCache
        log.debug("Clear packageCache");
        packageCache.clear();
    }

    /**
     * reset index
     */

    private void resetIndex() {
        log.debug("Reset index");
        index = 0;
    }

    private void addIndex() {
        log.debug("index add 1");
        index = index + 1;
    }

    /**
     * init snapshot after afterPropertiesSet
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //init all snapshot service
        init();
    }

    /**
     * buildValue
     *
     * @param object
     * @param status
     * @return
     */
    private Value buildValue(Object object, String status, int index) {
        return new Value(object, status, index);
    }

    /**
     * check for insert or update
     *
     * @param key1
     * @param key2
     * @param value
     */
    private void putCheck(SnapshotBizKeyEnum key1, Object key2, Object value) {
        //Check null
        if (null == key1 || null == key2) {
            log.error("Put data into snapshot ,the key1  and key2 can not be null, in fact key1 = {}, key2 = {}", key1, key2);
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION);
        }

        if (null == value) {
            log.error("The put data cant't be null");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION, "The value put into snapshot  is null pointed exception");
        }

        //check whether snapshot transaction has been started.
        isNotOpenTransactionException();

        //check where there is key1 in txCache, if not put a inner map as the value
        if (!txCache.containsKey(key1)) {
            log.debug("There is no  snapshotBizKeyEnum: {} in the txCache, we will put a inner map into txCache", key1);
            txCache.put(key1, new ConcurrentHashMap<>());
        }
    }

    /**
     * check is open transaction and throw exception
     */
    private void isOpenTransactionException() {
        if (isOpenTransaction) {
            log.debug("The snapshot transaction has been started ! So we can't deal with this action");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_TRANSACTION_HAS_STARTED_EXCEPTION);
        }
    }

    /**
     * check is not open transaction and throw exception
     */
    private void isNotOpenTransactionException() {
        if (!isOpenTransaction) {
            log.debug("The snapshot transaction has not been started ! So we can't deal with this action");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_TRANSACTION_NOT_STARTED_EXCEPTION);
        }
    }

    /**
     * serialize Json String
     * @param object
     * @return
     */
    private String serializeJsonString(Object object){
       return JsonUtils.serializeWithType(object);
    }
}
