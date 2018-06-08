package com.higgs.trust.slave.core.service.snapshot.agent;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.TxOutRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * UTXO snapshot agent
 *
 * @author lingchao
 * @create 2018年04月13日16:53
 */
@Slf4j
@Service
public class UTXOSnapshotAgent implements CacheLoader {

    @Autowired
    private TxOutRepository txOutRepository;

    @Autowired
    private SnapshotService snapshot;

    /**
     * get data from snapshot
     *
     * @param key
     * @param <T>
     * @return
     */
    private <T> T get(Object key) {
        return (T) snapshot.get(SnapshotBizKeyEnum.UTXO, key);
    }

    /**
     * put data to snapshot
     *
     * @param key
     * @param object
     */
    //TODO You  should provide insert and update method for yourself to use by using snapshot insert or uodate method .
    private void put(Object key, Object object) {
        // snapshot.put(SnapshotBizKeyEnum.UTXO, key, object);
    }

    /**
     * add data into snapshot
     * <p>
     * query UTXO by txId, index and actionIndex
     *
     * @param txId
     * @param index
     * @param actionIndex
     * @return
     */
    public UTXO queryUTXO(String txId, Integer index, Integer actionIndex) {
        TxOutCacheKey txOutCacheKey = new TxOutCacheKey(txId, index, actionIndex);
        TxOutPO txOutPO = get(txOutCacheKey);
        UTXO utxo = BeanConvertor.convertBean(txOutPO, UTXO.class);
        if (null != txOutPO) {
            utxo.setState(JSON.parseObject(txOutPO.getState()));
        }
        return utxo;
    }

    /**
     * @param txOutPOList
     */
    public boolean batchInsertTxOut(List<TxOutPO> txOutPOList) {
        for (TxOutPO txOutPO : txOutPOList) {
            TxOutCacheKey txOutCacheKey = new TxOutCacheKey(txOutPO.getTxId(), txOutPO.getIndex(), txOutPO.getActionIndex());
            put(txOutCacheKey, txOutPO);
        }
        return true;
    }

    /**
     * update data in the snapshot
     *
     * @param txOutPOList
     */
    public boolean bachUpdateTxOut(List<TxOutPO> txOutPOList) {
        for (TxOutPO txOutPO : txOutPOList) {
            TxOutCacheKey txOutCacheKey = new TxOutCacheKey(txOutPO.getTxId(), txOutPO.getIndex(), txOutPO.getActionIndex());
            put(txOutCacheKey, txOutPO);
        }
        return true;
    }

    /**
     * query from db
     *
     * @param object
     * @return
     */
    @Override
    public Object query(Object object) {
        TxOutCacheKey txOutCacheKey = (TxOutCacheKey) object;
        return txOutRepository.queryTxOut(txOutCacheKey.getTxId(), txOutCacheKey.getIndex(), txOutCacheKey.getActionIndex());
    }

    /**
     * the method to bachInsert data into db
     *
     * @param insertMap
     * @return
     */
    //TODO to implements your own bachInsert method for db
    @Override
    public boolean bachInsert(Map<Object, Object> insertMap) {
        return false;
    }

    /**
     * the method to bachUpdate data into db
     *
     * @param updateMap
     * @return
     */
    //TODO to implements your own bachUpdate method for db
    @Override
    public boolean bachUpdate(Map<Object, Object> updateMap) {
        return false;
    }

    /**
     * TxOutCacheKey
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TxOutCacheKey extends BaseBO {

        /**
         * transaction id
         */
        private String txId;
        /**
         * index for the out in the transaction
         */
        private Integer index;
        /**
         * index for the action of the out in the transaction
         */
        private Integer actionIndex;
    }


}
