package com.higgs.trust.slave.core.service.snapshot;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.api.enums.SnapshotValueStatusEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.exception.SnapshotException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.service.snapshot.agent.*;
import com.higgs.trust.slave.model.bo.snapshot.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
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
        log.info("Start to register cache loader, get lock for it");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to init snapshot!");
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

        log.info("End of register cache loader");
    }

    /**
     * start the snapshot transaction.Tag isOpenTransaction to be true.
     */
    @Override
    public void startTransaction() {
        Profiler.enter("[Snapshot.startTransaction]");
        log.info("Start to start snapshot transaction, and get lock for it");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to startTransaction!");
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
        log.info("End of start snapshot transaction");
        Profiler.release();
    }

    /**
     * clear packageCache and txCache
     */
    @Override
    public void clear() {
        Profiler.enter("[Snapshot.destroy]");
        log.info("Start to destroy snapshot");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to clear snapshot!");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_GET_NO_LOCK_EXCEPTION);
        }
        try {
            log.debug("Get lock success, go to clear snapshot!");
            //close transaction first,if not there may be some data put into cache after clearing data
            closeTransaction();

            //clear packageCache and txCache
            clearTempCache();

            //check whether snapshot transaction has been started.
            isOpenTransactionException();
        } finally {
            log.debug("Unlock lock for clear snapshot!");
            lock.unlock();
        }
        log.info("End of destroy snapshot");
        Profiler.release();
    }


    /**
     * clear globalCache and packageCache and txCache
     */
    @Override
    public void destroy() {
        Profiler.enter("[Snapshot.destroy]");
        log.info("Start to destroy snapshot");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to destroy snapshot!");
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

            //check whether snapshot transaction has been started.
            isOpenTransactionException();
        } finally {
            log.debug("Unlock lock for destroy snapshot!");
            lock.unlock();
        }
        log.info("End of destroy snapshot");
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
            log.info("Start to get data for snapshotBizKeyEnum:{}, bizKey:{}", key1, key2);
            //Check null
            if (null == key1 || null == key2) {
                log.error("Get data from snapshot ,the key1  and key2 can not be null, in fact key1 = {}, key2 = {}", key1, key2);
                throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION, "Put data into snapshot key1 and key2 can not be null!");
            }

            //get data from txCache
            Value value = getDataFromTxCache(key1, key2);
            if (null != value) {
                log.info("Get snapshotBizKeyEnum: {} , innerKey : {} , value :{} from  snapshot, it is in the txCache", key1, key2, value.getObject());
                return transferValue(value.getObject());
            }

            //get data from packageCache
            value = getDataFromPackageCache(key1, key2);
            if (null != value) {
                log.info("Get snapshotBizKeyEnum: {} , innerKey : {} , value :{} from  snapshot, it is in the packageCache", key1, key2, value.getObject());
                return transferValue(value.getObject());
            }

            //get data from globalCache
            Object object = getDataFromGlobalCache(key1, key2);

            log.info("End of get data for snapshotBizKeyEnum:{}, bizKey:{}", key1, key2);
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
            log.info("Start to insert data {}  for snapshotBizKeyEnum:{}, bizKey:{}", value, key1, key2);

            //check  before insert and update
            putCheck(key1, key2, value);

            log.debug("Insert snapshotBizKeyEnum: {} , innerKey : {} , value :{} into txCache", key1, key2, value);

            String innerKey = JSON.toJSONString(key2);
            ConcurrentHashMap<String, Value> innerMap = txCache.get(key1);

            //check whether there is data in the txCache
            if (null != innerMap.get(innerKey)) {
                log.error("Insert snapshotBizKeyEnum: {} , innerKey : {} , value :{} into txCache duplicate key exception", key1, key2, value);
                throw new DuplicateKeyException("Insert data into txCache duplicate key exception");
            }

            //put insert status data into txCache
            Value putValue = buildValue(value, SnapshotValueStatusEnum.INSERT.getCode());
            innerMap.put(innerKey, (Value) transferValue(putValue));

            //check whether snapshot transaction has been started.
            isNotOpenTransactionException();
            log.info("End of insert data {} into txCache for snapshotBizKeyEnum:{}, bizKey:{}", value, key1, key2);
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
            log.info("Start to update data {}  for snapshotBizKeyEnum:{}, bizKey:{}", value, key1, key2);

            //check  before insert and update
            putCheck(key1, key2, value);

            log.debug("Update snapshotBizKeyEnum: {} , innerKey : {} , value :{} into txCache", key1, key2, value);

            String innerKey = JSON.toJSONString(key2);
            ConcurrentHashMap<String, Value> innerMap = txCache.get(key1);

            //check whether there is data in the txCache
            Value objectExisted = innerMap.get(innerKey);

            Value putValue = null;

            //when object existed in txCache and the status is insert, we update object in txCache and stay insert status
            if (null != objectExisted && StringUtils.equals(SnapshotValueStatusEnum.INSERT.getCode(), objectExisted.getStatus())) {
                //put insert status data into txCache
                putValue = buildValue(value, SnapshotValueStatusEnum.INSERT.getCode());
            } else {
                //when object existed in txCache and the status is update
                // or when object is not existed in txCache,
                // we put object in txCache with  update status
                putValue = buildValue(value, SnapshotValueStatusEnum.UPDATE.getCode());
            }

            //put data into
            innerMap.put(innerKey, (Value) transferValue(putValue));
            //check whether snapshot transaction has been started.
            isNotOpenTransactionException();
            log.info("End of update data {} with status {} into txCache for snapshotBizKeyEnum:{}, bizKey:{}", value, putValue.getStatus(), key1, key2);
        } finally {
            Profiler.release();
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
        log.info("Start to commit");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to commit!");
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

            //check whether snapshot transaction has been started.
            isOpenTransactionException();
        } finally {
            log.debug("Unlock lock for commit!");
            lock.unlock();
            Profiler.release();
        }
        log.info("End of commit ");
    }

    /**
     * 1.clear txCache
     * 2.tag the isOpenTransaction to be false
     */
    @Override
    public void rollback() {
        Profiler.enter("[Snapshot.rollback]");
        log.info("Start to rollback");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to commit!");
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

            //check whether snapshot transaction has been started.
            isOpenTransactionException();
        } finally {
            log.info("Unlock lock for rollback!");
            lock.unlock();
            Profiler.release();
        }
        log.info("End of rollback");
    }

    /**
     * 1.flush data into globalCache
     * 2.flush data into db
     */
    @Override
    public void flush() {
        Profiler.enter("[Snapshot.flush]");
        log.info("Start to flush");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to flush!");
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

            //check whether snapshot transaction has been started.
            isOpenTransactionException();
        } finally {
            log.info("Unlock lock for flush!");
            lock.unlock();
            Profiler.release();
        }
        log.info("End of flush");
    }

    /**
     * 1.register  loading cache method  to guavaCache
     * 2.add guavaCache to globalCache
     * 3.add cacheLoader to cacheLoaderCache
     */
    //TODO lingchao make MAXIMUN_SIZE  config in the config file
    private void registerBizLoadingCache(SnapshotBizKeyEnum snapshotBizKeyEnum, CacheLoader cacheLoader) {
        log.info("Start to register core loadingCache to globalCache and cacheLoaderCache for snapshotBizKeyEnum:{}", snapshotBizKeyEnum);
        LoadingCache<String, Object> bizCache = CacheBuilder.newBuilder().initialCapacity(10).maximumSize(GLOBAL_MAXIMUN_SIZE).refreshAfterWrite(REFRESH_TIME, TimeUnit.DAYS).build(new com.google.common.cache.CacheLoader<String, Object>() {
            @Override
            public Object load(String bo) throws Exception {
                log.info("There is no data for  bizKey： {}  by snapshotBizKeyEnum： {} in globalCache ,try to get data from DB", bo, snapshotBizKeyEnum);
                //just want to get Clazz
                Object object = JSON.parse(bo);
                return cacheLoader.query(object);
            }
        });

        //add guavaCache to globalCache
        globalCache.put(snapshotBizKeyEnum, bizCache);

        // add cacheLoader to cacheLoaderCache
        cacheLoaderCache.put(snapshotBizKeyEnum, cacheLoader);
        log.info("End of register core loadingCache to globalCache and cacheLoaderCache for snapshotBizKeyEnum:{}", snapshotBizKeyEnum);
    }

    /**
     * close transaction
     */
    private void closeTransaction() {
        log.info("Close the snapshot transaction");
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
        String innerKey = JSON.toJSONString(key2);
        //check where there is key1 in txCache
        if (!txCache.containsKey(key1)) {
            log.info("There is no  snapshotBizKeyEnum: {} in the txCache ", key1);
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

        String innerKey = JSON.toJSONString(key2);
        //check where there is key1 in packageCache
        if (!packageCache.containsKey(key1)) {
            log.info("There is no  snapshotBizKeyEnum: {} in the packageCache ", key1);
            return null;
        }
        //get data from packageCacheInnerMap
        ConcurrentHashMap<String, Value> txCacheInnerMap = packageCache.get(key1);
        return txCacheInnerMap.get(innerKey);
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
            //TODO lingchao 加监控
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION);
        }
        //1.Get data from globalCache.It will return data when there is data in the globalCache
        //2.else it will get data from db .
        //3.If there is no data in db ,it will return null
        //4.else return data and put data into globalCache
        Object value = null;
        String innerKey = JSON.toJSONString(key2);
        LoadingCache<String, Object> innerMap = globalCache.get(key1);
        try {
            value = innerMap.get(innerKey);
            log.info("Get snapshotBizKeyEnum: {} , innerKey : {} , value :{} from  snapshot, it is from  the globalCache", key1, innerKey, value);
        } catch (Throwable e) {
            if (!(e instanceof com.google.common.cache.CacheLoader.InvalidCacheLoadException)) {
                log.error("There is  exception happened to query data for  snapshotBizKeyEnum: {} , innerKey : {}  in  snapshot and db", key1, innerKey, e);
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_QUERY_EXCEPTION, e);
            }
            log.info("There is no data for  snapshotBizKeyEnum: {} , innerKey : {}  in  snapshot and db", key1, innerKey);
        }
        return value;
    }


    /**
     * copy data from txCache to packageCache
     */
    private void copyDataToPackageCache() {
        log.info("Start to copy data from txCache to packageCache");
        for (Map.Entry<SnapshotBizKeyEnum, ConcurrentHashMap<String, Value>> outerEntry : txCache.entrySet()) {

            SnapshotBizKeyEnum snapshotBizKeyEnum = outerEntry.getKey();
            ConcurrentHashMap<String, Value> innerMap = outerEntry.getValue();
            //check whether there is snapshotBizKeyEnum in packageCache
            if (!packageCache.containsKey(snapshotBizKeyEnum)) {
                log.info("There is no key:{} in packageCache!", snapshotBizKeyEnum);
                packageCache.put(snapshotBizKeyEnum, new ConcurrentHashMap<>());
            }

            //check whether inner map is empty.
            if (innerMap.isEmpty()) {
                log.info("The inner map for Snapshot txCache key :  {}  is empty. jump to next txCache key", snapshotBizKeyEnum);
                continue;
            }

            // foreach inner map to copy data
            ConcurrentHashMap<String, Value> innerCache = packageCache.get(snapshotBizKeyEnum);
            for (Map.Entry<String, Value> innerEntry : innerMap.entrySet()) {
                //check  cache size
                if (innerCache.size() >= PACKAGE_MAXIMUN_SIZE) {
                    log.error("Cache size  : {} for key:{} in packageCache is equal or bigger than {}!", innerCache.size(), snapshotBizKeyEnum, PACKAGE_MAXIMUN_SIZE);
                    //TODO lingchao 加监控
                    throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_CACHE_SIZE_NOT_ENOUGH_EXCEPTION);
                }

                //put data into package inner cache
                putDataIntoPackageCache(innerCache, innerEntry, snapshotBizKeyEnum);
            }
        }
        log.info("End  of copy data from txCache to packageCache");
    }


    /**
     * put data into package inner cache
     *
     * @param packageInnerCache
     * @param txCacheInnerEntry
     * @param snapshotBizKeyEnum
     */
    //TODO lingchao check whether cover all case to put into data
    private void putDataIntoPackageCache(ConcurrentHashMap<String, Value> packageInnerCache, Map.Entry<String, Value> txCacheInnerEntry, SnapshotBizKeyEnum snapshotBizKeyEnum) {

        String innerKey = txCacheInnerEntry.getKey();
        Value innerValue = txCacheInnerEntry.getValue();

        Object keyObject = JSON.parse(innerKey);
        //txCache value is insert status
        boolean isInsert = StringUtils.equals(innerValue.getStatus(), SnapshotValueStatusEnum.INSERT.getCode());
        //data in PackageCache
        Value packageValue = getDataFromPackageCache(snapshotBizKeyEnum, keyObject);
        boolean isExistedInPackageCache = null != packageValue;
        //data in GlobalCache
        Object globalValue = getDataFromGlobalCache(snapshotBizKeyEnum, keyObject);
        boolean isExistedInGlobalCache = null != globalValue;
        //data in PackageCache or GlobalCache or DB
        boolean isExistedInPackageOrGlobalOrDB = isExistedInPackageCache || isExistedInGlobalCache;

        log.info("Put SnapshotBizKeyEnum: {} , innerKey : {} , value :{} into packageCache", snapshotBizKeyEnum, innerKey, innerValue);

        //if the txCache value is insert status and there is data in package cache or global cache or db, it is a exception
        if (isInsert && isExistedInPackageOrGlobalOrDB) {
            log.error("Insert snapshotBizKeyEnum: {} , innerKey : {} into packageCache duplicate key exception", snapshotBizKeyEnum, innerKey);
            throw new DuplicateKeyException("Insert data into packageCache duplicate key exception");
        }

        //if the txCache value is insert status and there is no data in package cache or global cache or db, put data into packageCache
        if (isInsert && !isExistedInPackageOrGlobalOrDB) {
            packageInnerCache.put(innerKey, innerValue);
            return;
        }


        // check whether data in db
        boolean isExistedInDB = isExistInDB(snapshotBizKeyEnum, keyObject);
        //if the txCache value is update status and there is no data in package cache and global cache and db, throw exception
        //it is not exist in db and status is not update , step will not go to last. so we do not need to check  again.
        if (!isExistedInDB){
            log.error("Update snapshotBizKeyEnum: {} , innerKey : {} into packageCache exception, the data to be updated is not exist in packageCache and globalCache and DB", snapshotBizKeyEnum, innerKey);
            throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_DATA_NOT_EXIST_EXCEPTION, "Update data into packageCache  exception, the data to be updated is not exist in  DB");
        }

        //if the txCache value is update status and there is data in package cache and  db, and package status is insert . put data into packageCache  with insert status
        if (isExistedInPackageCache && StringUtils.equals(packageValue.getStatus(), SnapshotValueStatusEnum.INSERT.getCode())) {
            packageInnerCache.put(innerKey, buildValue(innerValue.getObject(), SnapshotValueStatusEnum.INSERT.getCode()));
            return;
        }

        //if the txCache value is update status and there is data in package cache and  db, and package status is update . put data into packageCache
        if (isExistedInPackageCache && !StringUtils.equals(packageValue.getStatus(), SnapshotValueStatusEnum.INSERT.getCode())) {
            packageInnerCache.put(innerKey, innerValue);
            return;
        }

        //if the txCache value is update status and there is no data in package cache ,and has data in db . put data into packageCache
        if (!isExistedInPackageCache) {
            packageInnerCache.put(innerKey, innerValue);
            return;
        }

    }


    /**
     * check whether the data is existed in db
     * @param snapshotBizKeyEnum
     * @param key
     * @return
     */
    private boolean isExistInDB(SnapshotBizKeyEnum snapshotBizKeyEnum, Object key){
        //check whether there is snapshotBizKeyEnum in cacheLoaderCache
        if (!cacheLoaderCache.containsKey(snapshotBizKeyEnum)) {
            log.error("There is no key:{} for cacheLoader in cacheLoaderCache!", snapshotBizKeyEnum);
            //TODO lingchao 加监控
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION);
        }
        //cacheLoader to query data
        CacheLoader cacheLoader = cacheLoaderCache.get(snapshotBizKeyEnum);
        return null != cacheLoader.query(key);
    }

    /**
     * copy data from packageCache to globalCache
     */
    private void copyDataToGlobalCache() {
        log.info("Start to copy data from packageCache to globalCache");
        for (Map.Entry<SnapshotBizKeyEnum, ConcurrentHashMap<String, Value>> outerEntry : packageCache.entrySet()) {

            SnapshotBizKeyEnum snapshotBizKeyEnum = outerEntry.getKey();
            ConcurrentHashMap<String, Value> innerMap = outerEntry.getValue();
            //check whether there is snapshotBizKeyEnum in globalCache
            if (!globalCache.containsKey(snapshotBizKeyEnum)) {
                log.error("There is no key:{} in globalCache!", snapshotBizKeyEnum);
                //TODO lingchao 加监控
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION);
            }

            //check whether inner map is empty.
            if (outerEntry.getValue().isEmpty()) {
                log.info("The inner map for Snapshot core key :  {}  is empty. jump to next core key", snapshotBizKeyEnum);
                continue;
            }

            // foreach inner map to copy data
            LoadingCache<String, Object> innerCache = globalCache.get(snapshotBizKeyEnum);
            for (Map.Entry<String, Value> innerEntry : innerMap.entrySet()) {
                //check  cache size
                if (innerCache.size() >= GLOBAL_MAXIMUN_SIZE) {
                    log.error("Cache size  : {} for key:{} in globalCache is equal or bigger than {}!", innerCache.size(), snapshotBizKeyEnum, GLOBAL_MAXIMUN_SIZE);
                    //TODO lingchao 加监控
                    throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_CACHE_SIZE_NOT_ENOUGH_EXCEPTION);
                }

                Value value = innerEntry.getValue();

                //if data status is insert and there is data in global cache or db
                if (StringUtils.equals(value.getStatus(), SnapshotValueStatusEnum.INSERT.getCode()) && null != getDataFromGlobalCache(snapshotBizKeyEnum, JSON.parse(innerEntry.getKey()))) {
                    log.error("Insert snapshotBizKeyEnum: {} , innerKey : {} into globalCache exception, the data to be insert is  exist in globalCache or DB", snapshotBizKeyEnum, innerEntry.getKey());
                    throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_DATA_EXIST_EXCEPTION, "Insert data into globalCache  exception, the data to be insert is  existed in globalCache or DB");
                }

                log.info("Put SnapshotBizKeyEnum: {} , innerKey : {} , value :{} into globalCache", snapshotBizKeyEnum, innerEntry.getKey(), innerEntry.getValue());
                //put data into global cache
                innerCache.put(innerEntry.getKey(), value.getObject());
            }
        }
        log.info("End  of copy data from txCache to globalCache");
    }

    /**
     * flush data into db
     */
    private void flushDataIntoDB() {
        log.info("Start to flush data into db");
        for (Map.Entry<SnapshotBizKeyEnum, ConcurrentHashMap<String, Value>> outerEntry : packageCache.entrySet()) {
            //key and value
            SnapshotBizKeyEnum snapshotBizKeyEnum = outerEntry.getKey();
            ConcurrentHashMap<String, Value> innerMap = outerEntry.getValue();

            //check whether inner map is empty.
            if (innerMap.isEmpty()) {
                log.info("The inner map for Snapshot core key :  {}  is empty. jump to next core key", snapshotBizKeyEnum);
                continue;
            }

            //check whether there is snapshotBizKeyEnum in cacheLoaderCache
            if (!cacheLoaderCache.containsKey(snapshotBizKeyEnum)) {
                log.error("There is no key:{} for cacheLoader in cacheLoaderCache!", snapshotBizKeyEnum);
                //TODO lingchao 加监控
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION);
            }

            //cacheLoader to  flush data to db
            CacheLoader cacheLoader = cacheLoaderCache.get(snapshotBizKeyEnum);
            //all insert data for the biz
            Map<Object, Object> insertMap = new HashMap<>();
            //all update data for the biz
            Map<Object, Object> updateMap = new HashMap<>();
            // foreach inner map to flush data
            for (Map.Entry<String, Value> innerEntry : innerMap.entrySet()) {
                Value value = innerEntry.getValue();
                Object keyObject = JSON.parse(innerEntry.getKey());
                //if data status is insert put it into  insertMap  else into updateMap
                if (StringUtils.equals(value.getStatus(), SnapshotValueStatusEnum.INSERT.getCode())) {
                    insertMap.put(keyObject, value.getObject());
                } else {
                    updateMap.put(keyObject, value.getObject());
                }
                log.info("Put SnapshotBizKeyEnum: {} , innerKey : {} , value :{} into flush map ", snapshotBizKeyEnum, innerEntry.getKey(), value);
            }
            try {

                //flush insert data into db
                if (!insertMap.isEmpty() && !cacheLoader.batchInsert(insertMap)) {
                    log.error("Flush data for batchInsert db failed error");
                    throw new SnapshotException(SlaveErrorEnum.SLAVE_DATA_NOT_INSERT_EXCEPTION);
                }

                //flush update data into db
                if (!updateMap.isEmpty() && !cacheLoader.batchUpdate(updateMap)) {
                    log.error("Flush data for batchUpdate db failed error");
                    throw new SnapshotException(SlaveErrorEnum.SLAVE_DATA_NOT_UPDATE_EXCEPTION);
                }
            } catch (Throwable e) {
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_FLUSH_DATA_EXCEPTION);
            }
        }
        log.info("End of flushing data into db");
    }

    /**
     * transfer the data to a different object
     *
     * @param object
     * @return
     */
    private Object transferValue(Object object) {
        String valueTemp = JSON.toJSONString(object);
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
    private Value buildValue(Object object, String status) {
        return new Value(object, status);
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
            log.info("There is no  snapshotBizKeyEnum: {} in the txCache, we will put a inner map into txCache", key1);
            txCache.put(key1, new ConcurrentHashMap<>());
        }
    }

    /**
     * check is open transaction and throw exception
     */
    private void isOpenTransactionException(){
        if (isOpenTransaction) {
            log.info("The snapshot transaction has been started ! So we can't deal with rollback");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_TRANSACTION_HAS_STARTED_EXCEPTION);
        }
    }

    /**
     * check is not open transaction and throw exception
     */
    private void isNotOpenTransactionException(){
        if (!isOpenTransaction) {
            log.info("The snapshot transaction has not been started ! So we can't deal with rollback");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_TRANSACTION_NOT_STARTED_EXCEPTION);
        }
    }
}
