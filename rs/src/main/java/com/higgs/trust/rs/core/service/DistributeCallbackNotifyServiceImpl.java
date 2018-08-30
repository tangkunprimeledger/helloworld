package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.DistributeCallbackNotifyService;
import com.higgs.trust.rs.core.api.enums.RedisMegGroupEnum;
import com.higgs.trust.rs.core.api.enums.RedisTopicEnum;
import com.higgs.trust.rs.core.bo.RedisTopicMsg;
import com.higgs.trust.rs.core.bo.WaitAsyncFinishLockObject;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * DistributeCallbackNotify
 *
 * @author lingchao
 * @create 2018年08月23日14:27
 */
@Slf4j
@Service
public class DistributeCallbackNotifyServiceImpl implements DistributeCallbackNotifyService, InitializingBean {
    private Cache<String, WaitAsyncFinishLockObject> lockCache = CacheBuilder.newBuilder().weakValues().build();
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        //add callback message notify
        RTopic<String> topic = redissonClient.getTopic(RedisTopicEnum.CALLBACK_MESSAGE_NOTIFY.getCode());
        topic.addListener(((channel, msg) -> {
            RedisTopicMsg redisTopicMsg = JSON.parseObject(msg, RedisTopicMsg.class);
            processNotify(redisTopicMsg);
        }));
    }

    /**
     * notify callback finish
     *
     * @param txId
     * @param respData
     * @param redisMegGroupEnum
     */
    @Override
    public void notifySyncResult(String txId, RespData respData, RedisMegGroupEnum redisMegGroupEnum) {
        publishTopic(txId, respData, redisMegGroupEnum);
    }

    /**
     * sync wait RespData
     *
     * @param key
     * @param redisMegGroupEnum
     * @param timeout
     * @param timeUnit
     * @return
     */
    @Override
    public RespData syncWaitNotify(String key, RedisMegGroupEnum redisMegGroupEnum, long timeout, TimeUnit timeUnit) {
        String uniqueKey = buildUniqueKey(redisMegGroupEnum, key);
        WaitAsyncFinishLockObject lockObject = createOrGetLockObj(uniqueKey);
        long remain = timeUnit.toNanos(timeout);
        WaitAsyncFinishLockObject finalLockObject = null;

        Lock lock = lockObject.getLock();
        lock.lock();
        try {
            Pair<Long, WaitAsyncFinishLockObject> pair = waitNotify(remain, lockObject, finalLockObject, uniqueKey);
            remain = pair.getLeft().longValue();
            finalLockObject = pair.getRight();
        } finally {
            lock.unlock();
        }

        if (remain <= 0) {
            String message = MessageFormatter.format("wait async finish timeout, timeout={} ,timeUnit={}", timeout, timeUnit).getMessage();
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_WAIT_ASYNC_TIMEOUT_EXCEPTION, message);
        }
        return JSON.parseObject(finalLockObject.getResult().toString(), RespData.class);

    }

    /**
     * wait notify
     *
     * @param remain
     * @param lockObject
     * @param finalLockObject
     * @param uniqueKey
     * @return
     */
    private Pair<Long, WaitAsyncFinishLockObject> waitNotify(long remain, WaitAsyncFinishLockObject lockObject, WaitAsyncFinishLockObject finalLockObject, String uniqueKey) {
        // wait notify
        while (remain > 0) {
            // check weather finish
            finalLockObject = lockCache.getIfPresent(uniqueKey);
            if (null != finalLockObject && finalLockObject.isFinish()) {
                break;
            }

            try {
                remain = lockObject.getCondition().awaitNanos(remain);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        return Pair.of(remain, finalLockObject);
    }


    /**
     * process otify
     *
     * @param redisTopicMsg
     */
    private void processNotify(RedisTopicMsg redisTopicMsg) {
        String uniqueKey = buildUniqueKey(redisTopicMsg.getRedisMegGroupEnum(), redisTopicMsg.getKey());
        WaitAsyncFinishLockObject lockObject = createOrGetLockObj(uniqueKey);
        // notify
        Lock lock = lockObject.getLock();
        lock.lock();
        try {
            if (!lockObject.isFinish()) {
                lockObject.setResult(redisTopicMsg.getResult());
                lockObject.getCondition().signalAll();
                lockObject.setFinish(true);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * publish  redis topic
     *
     * @param txId
     * @param respData
     * @param redisMegGroupEnum
     */
    private void publishTopic(String txId, RespData respData, RedisMegGroupEnum redisMegGroupEnum) {
        RTopic<String> topic = redissonClient.getTopic(RedisTopicEnum.CALLBACK_MESSAGE_NOTIFY.getCode());
        RedisTopicMsg redisTopicMsg = new RedisTopicMsg(txId, respData, redisMegGroupEnum);
        String message = JSON.toJSONString(redisTopicMsg);
        topic.publish(message);
    }

    /**
     * createOrGetLockObj
     *
     * @param uniqueKey
     * @return
     */
    private WaitAsyncFinishLockObject createOrGetLockObj(String uniqueKey) {
        WaitAsyncFinishLockObject lockObj;
        try {
            lockObj = lockCache.get(uniqueKey, () -> {
                WaitAsyncFinishLockObject newLockObj = new WaitAsyncFinishLockObject();
                Lock lock = new ReentrantLock();
                newLockObj.setFinish(false);
                newLockObj.setCondition(lock.newCondition());
                newLockObj.setLock(lock);
                return newLockObj;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return lockObj;
    }

    /**
     * build unique key
     *
     * @param redisMegGroupEnum
     * @param key
     * @return
     */
    private String buildUniqueKey(RedisMegGroupEnum redisMegGroupEnum, String key) {
        return redisMegGroupEnum.getCode() + "|" + key;
    }
}
