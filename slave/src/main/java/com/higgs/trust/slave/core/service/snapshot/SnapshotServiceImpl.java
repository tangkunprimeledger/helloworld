package com.higgs.trust.slave.core.service.snapshot;


import cn.primeledger.stability.log.TraceMonitor;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.exception.SnapshotException;
import com.higgs.trust.slave.core.service.snapshot.agent.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class SnapshotServiceImpl implements SnapshotService,InitializingBean {
    /**
     * tag whether  the snapshot is in  transaction
     */
    private static boolean isOpenTransaction = false;

    private static final int MAXIMUN_SIZE = 10000;

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

    /**
     * cache  for package
     */
    private ConcurrentHashMap<SnapshotBizKeyEnum, LoadingCache<String, Object>> packageCache = new ConcurrentHashMap<>();
    /**
     * cache for transaction
     */
    private ConcurrentHashMap<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>> txCache = new ConcurrentHashMap<>();

    /**
     * register all the caches to packageSnapshot,only run when the application is starting.
     */
    @Override
    public void init() {
        log.info("Start to register cache loader, get lock for it");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to init snapshot!");
            return;
        }
        try {
            log.info("Get lock success, go init cache!");
            log.info(("Clear txCache and packageCache first"));
            txCache.clear();
            packageCache.clear();
            //register UTXO cache loader
            log.info("Register UTXO cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.UTXO, utxoSnapshotAgent);

            //register MERKLE_TREE cache loader
            log.info("Register MERKLE_TREE cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.MERKLE_TREE, merkleTreeSnapshotAgent);

            //register MANAGE cache loader
            log.info("Register MANAGE cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.MANAGE, manageSnapshotAgent);

            //register DATA_IDENTITY cache loader
            log.info("Register DATA_IDENTITY cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.DATA_IDENTITY, dataIdentitySnapshotAgent);

            //register ACCOUNT cache loader
            log.info("Register ACCOUNT cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.ACCOUNT, accountSnapshotAgent);

            //register ACCOUNT cache loader
            log.info("Register FREEZE cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.FREEZE, freezeSnapshotAgent);

            //register CONTRACT cache loader
            log.info("Register CONTRACT cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.CONTRACT, contractSnapshotAgent);

            //register ACCOUNT_CONTRACT_BIND cache loader
            log.info("Register ACCOUNT_CONTRACT_BIND cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.ACCOUNT_CONTRACT_BIND, accountContractBindingSnapshotAgent);

            //register CONTRACT STATE cache loader
            log.info("Register CONTRACT STATE cache loader");
            registerBizLoadingCache(SnapshotBizKeyEnum.CONTRACT_SATE, contractStateSnapshotAgent);
        } finally {
            log.info("unlock lock  for init snapshot");
            lock.unlock();
        }

        log.info("End of register cache loader");
    }

    /**
     * start the snapshot transaction.Tag isOpenTransaction to be true.
     */
    @Override
    public void startTransaction() {
        log.info("Start to start snapshot transaction, and get lock for it");

        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to startTransaction!");
            return;
        }
        try {
            log.info("Get lock success, go to start transaction!");
            //check whether snapshot transaction has been started.
            if (isOpenTransaction) {
                log.info("The snapshot transaction has been started ! Please don't start it again!");
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_TRANSACTION_HAS_STARTED_EXCEPTION);
            }

            //clear txCache
            log.info("Clear txCache");
            txCache.clear();

            //sign it as in transaction
            isOpenTransaction = true;
        } finally {
            log.info("unlock lock for startTransaction");
            lock.unlock();
        }

        log.info("End of start snapshot transaction");
    }

    /**
     * clear packageCache and txCache
     */
    @TraceMonitor @Override
    public void destroy() {

        //TODO  是否加个标记，不允许其他操作
        log.info("Start to destroy snapshot");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to destroy snapshot!");
            return;
        }
        try {
            log.info("Get lock success, go to destroy snapshot!");
            //close transaction first,if not there may be some data put into cache after clearing data
            closeTransaction();

            //clear txCache
            log.info("Clear txCache");
            txCache.clear();

            //check whether there is data in the packageCache
            log.info("Clear packageCache");
            if (packageCache.isEmpty()) {
                log.error("There snapshot have not init");
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_NOT_INIT_EXCEPTION);
            }

            //clear packageCache inner cache
            for (Map.Entry<SnapshotBizKeyEnum, LoadingCache<String, Object>> outerEntry : packageCache.entrySet()) {
                log.info("Clear snapshotBizKeyEnum: {}  from  packageCache", outerEntry.getKey());
                LoadingCache<String, Object> innerMap = outerEntry.getValue();
                innerMap.invalidateAll();
            }
        } finally {
            log.info("Unlock lock for destroy snapshot!");
            lock.unlock();
        }
        log.info("End of destroy snapshot");
    }

    /**
     * get object from snapshot cache .It will get it from txCache first.
     * If there is not object ,then we will get it from db.
     *
     * @param key1
     * @param key2
     * @return
     */
    @Override
    public Object get(SnapshotBizKeyEnum key1, Object key2) {
        log.info("Start to get data for snapshotBizKeyEnum:{}, bizKey:{}", key1, key2);
        //Check null
        if (null == key1 || null == key2) {
            log.error("Get data from snapshot ,the key1  and key2 can not be null, in fact key1 = {}, key2 = {}", key1, key2);
            throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION, "Put data into snapshot key1 and key2 can not be null!");
        }

        //get data from txCache
        Object value = getDataFromTxCache(key1, key2);
        if (null != value) {
            log.info("Get snapshotBizKeyEnum: {} , innerKey : {} , value :{} from  snapshot, it is in the txCache", key1, key2, value);
            return transferValue(value);
        }

        //get data from packageCache
        value = getDataFromPackageCache(key1, key2);

        log.info("End of get data for snapshotBizKeyEnum:{}, bizKey:{}", key1, key2);
        if (null == value) {
            return value;
        }
        return transferValue(value);
    }

    /**
     * put object into the snapshot  txCache
     *
     * @param key1
     * @param key2
     */
    @Override
    public void put(SnapshotBizKeyEnum key1, Object key2, Object value) {
        log.info("Start to put data {}  for snapshotBizKeyEnum:{}, bizKey:{}", value, key1, key2);
        //Check null
        if (null == key1 || null == key2) {
            log.error("Get data from snapshot ,the key1  and key2 can not be null, in fact key1 = {}, key2 = {}", key1, key2);
            throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION);
        }

        if (null == value) {
            log.error("The put data cant't be null");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION, "The value putted into snapshot  is null pointed exception");
        }

        //check whether snapshot transaction has been started.
        if (!isOpenTransaction) {
            log.info("The snapshot transaction has not been started ! So we can't deal with put data operation");
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_TRANSACTION_NOT_STARTED_EXCEPTION);
        }

        //check where there is key1 in txCache, if not put a inner map as the value
        if (!txCache.containsKey(key1)) {
            log.info("There is no  snapshotBizKeyEnum: {} in the txCache, we will put a inner map into txCache", key1);
            txCache.put(key1, new ConcurrentHashMap<>());
        }

        log.info("Put SnapshotBizKeyEnum: {} , innerKey : {} , value :{} into txCache", key1, key2, value);
        String innerKey = JSON.toJSONString(key2);
        ConcurrentHashMap<String, Object> innerMap = txCache.get(key1);
        innerMap.put(innerKey, transferValue(value));

        log.info("End of put data {}  for snapshotBizKeyEnum:{}, bizKey:{}", value, key1, key2);
    }


    /**
     * 1. copy the txCache to packageCache
     * 2.clear txCache
     * 3.tag the isOpenTransaction to be false
     */
    @TraceMonitor @Override
    public void commit() {
        log.info("Start to commit");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to commit!");
            return;
        }
        try {
            log.info("Get lock success, go to commit!");
            //check whether snapshot transaction has been started.
            if (!isOpenTransaction) {
                log.info("The snapshot transaction has not been started ! So we can't deal with rollback");
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_TRANSACTION_NOT_STARTED_EXCEPTION);
            }

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
            log.info("Unlock lock for commit!");
            lock.unlock();
        }
        log.info("End of commit ");
    }

    /**
     * 1.clear txCache
     * 2.tag the isOpenTransaction to be false
     */
    @Override
    public void rollback() {
        log.info("Start to rollback");
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("Get lock failed, stop to commit!");
            return;
        }
        try {
            log.info("Get lock success, go to rollback!");
            //check whether snapshot transaction has been started.
            if (!isOpenTransaction) {
                log.info("The snapshot transaction has not been started ! So we can't deal with rollback");
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_TRANSACTION_NOT_STARTED_EXCEPTION);
            }

            //close transaction,if not there may be some data put into cache after clearing data
            closeTransaction();

            //clear txCache
            txCache.clear();
        } finally {
            log.info("Unlock lock for rollback!");
            lock.unlock();
        }
        log.info("End of rollback");
    }


    /**
     * 1.register  loading cache method  to guavaCache
     * 2.add guavaCache to packageCache
     */
    //TODO lingchao make MAXIMUN_SIZE  config in the config file
    private void registerBizLoadingCache(SnapshotBizKeyEnum snapshotBizKeyEnum, CacheLoader cacheLoader) {
        log.info("Start to register core loadingCache to packageCache for snapshotBizKeyEnum:{}", snapshotBizKeyEnum);
        LoadingCache<String, Object> bizCache = CacheBuilder.newBuilder().initialCapacity(10).maximumSize(MAXIMUN_SIZE).refreshAfterWrite(REFRESH_TIME, TimeUnit.DAYS).build(new com.google.common.cache.CacheLoader<String, Object>() {
            @Override
            public Object load(String bo) throws Exception {
                log.info("There is no data for  bizKey： {}  by snapshotBizKeyEnum： {} in packageCache ,try to get data from DB", bo, snapshotBizKeyEnum);
                //just want to get Clazz
                Object object = JSON.parse(bo);
                return cacheLoader.query(object);
            }
        });
        packageCache.put(snapshotBizKeyEnum, bizCache);
        log.info("End of register core loadingCache to packageCache for snapshotBizKeyEnum:{}", snapshotBizKeyEnum);
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
    private Object getDataFromTxCache(SnapshotBizKeyEnum key1, Object key2) {
        String innerKey = JSON.toJSONString(key2);
        //check where there is key1 in txCache
        if (!txCache.containsKey(key1)) {
            log.info("There is no  snapshotBizKeyEnum: {} in the txCache ", key1);
            return null;
        }
        //get data from txCacheInnerMap
        ConcurrentHashMap<String, Object> txCacheInnerMap = txCache.get(key1);
        return txCacheInnerMap.get(innerKey);
    }

    /**
     * get data from packageCache
     *
     * @param key1
     * @param key2
     * @return
     */
    private Object getDataFromPackageCache(SnapshotBizKeyEnum key1, Object key2) {

        //check whether there is snapshotBizKeyEnum in packageCache
        if (!packageCache.containsKey(key1)) {
            log.error("There is no key:{} for bizCache in packageCache!", key1);
            //TODO lingchao 加监控
            throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION);
        }
        //1.Get data from packageCache.It will return data when there is data in the packageCache
        //2.else it will get data from db .
        //3.If there is no data in db ,it will return null
        //4.else return data and put data into packageCache
        Object value = null;
        String innerKey = JSON.toJSONString(key2);
        LoadingCache<String, Object> innerMap = packageCache.get(key1);
        try {
            value = innerMap.get(innerKey);
            log.info("Get snapshotBizKeyEnum: {} , innerKey : {} , value :{} from  snapshot, it is from  the packageCache", key1, innerKey, value);
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
        for (Map.Entry<SnapshotBizKeyEnum, ConcurrentHashMap<String, Object>> outerEntry : txCache.entrySet()) {

            SnapshotBizKeyEnum snapshotBizKeyEnum = outerEntry.getKey();
            ConcurrentHashMap<String, Object> innerMap = outerEntry.getValue();
            //check whether there is snapshotBizKeyEnum in packageCache
            if (!packageCache.containsKey(snapshotBizKeyEnum)) {
                log.error("There is no key:{} in packageCache!", snapshotBizKeyEnum);
                //TODO lingchao 加监控
                throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION);
            }

            //check whether inner map is empty.
            if (outerEntry.getValue().isEmpty()) {
                log.info("The inner map for Snapshot core key :  {}  is empty. jump to next core key", snapshotBizKeyEnum);
                continue;
            }

            // foreach inner map to copy data
            LoadingCache<String, Object> innerCache = packageCache.get(snapshotBizKeyEnum);
            for (Map.Entry<String, Object> innerEntry : innerMap.entrySet()) {
                //check  cache size
                if (innerCache.size() >= MAXIMUN_SIZE) {
                    log.error("Cache size  : {} for key:{} in packageCache is equal or bigger than {}!", innerCache.size(), snapshotBizKeyEnum, MAXIMUN_SIZE);
                    //TODO lingchao 加监控
                    throw new SnapshotException(SlaveErrorEnum.SLAVE_SNAPSHOT_CACHE_SIZE_NOT_ENOUGH_EXCEPTION);
                }
                log.info("Put SnapshotBizKeyEnum: {} , innerKey : {} , value :{} into packageCache", snapshotBizKeyEnum, innerEntry.getKey(), innerEntry.getValue());
                innerCache.put(innerEntry.getKey(), innerEntry.getValue());
            }
        }
        log.info("End  of copy data from txCache to packageCache");
    }

    /**
     * transfer the data to a different object
     *
     * @param object
     * @return
     */
    //TODO lingchao need  a better way. it is  heavy for this
    private Object transferValue(Object object) {
        String valueTemp = JSON.toJSONString(object);
        return JSON.parseObject(valueTemp, object.getClass());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //init all snapshot service
        init();
    }

}
