package com.higgs.trust.slave.core.service.snapshot.agent;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.exception.SnapshotException;
import com.higgs.trust.slave.core.repository.TxOutRepository;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXODBHandler;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired
    private UTXODBHandler utxodbHandler;

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
     * insert  object into the snapshot
     * @param key
     * @param value
     */
   private void insert(Object key, Object value){
       snapshot.insert(SnapshotBizKeyEnum.UTXO, key, value);
   }

    /**
     * update  object into the snapshot
     * @param key
     * @param value
     */
    private void update(Object key, Object value){
        snapshot.update(SnapshotBizKeyEnum.UTXO, key, value);
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
            insert(txOutCacheKey, txOutPO);
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
            update(txOutCacheKey, txOutPO);
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
        if (!(object instanceof  TxOutCacheKey)){
            log.error("object {} is not the type of TxOutCacheKey error", object);
            throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_DATA_TYPE_ERROR_EXCEPTION);
        }
        TxOutCacheKey txOutCacheKey = (TxOutCacheKey) object;
        return txOutRepository.queryTxOut(txOutCacheKey.getTxId(), txOutCacheKey.getIndex(), txOutCacheKey.getActionIndex());
    }

    /**
     * the method to batchInsert data into db
     *
     * @param insertList
     * @return
     */
    @Override
    public boolean batchInsert(List<Pair<Object, Object>> insertList) {
        if (CollectionUtils.isEmpty(insertList)){
            return true;
        }

        //get bach insert data
        List<TxOutPO> txOutPOList = new ArrayList<>();
        for (Pair<Object, Object> pair : insertList) {
            if (!(pair.getLeft() instanceof  TxOutCacheKey)){
                log.error("insert key is not the type of TxOutCacheKey error");
                throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_DATA_TYPE_ERROR_EXCEPTION);
            }
            txOutPOList.add((TxOutPO)pair.getRight());
        }

        return utxodbHandler.batchInsert(txOutPOList);
    }

    /**
     * the method to batchUpdate data into db
     *
     * @param updateList
     * @return
     */
    @Override
    public boolean batchUpdate(List<Pair<Object, Object>> updateList) {
        if (CollectionUtils.isEmpty(updateList)){
            return true;
        }

        //get bach update data
        List<TxOutPO> txOutPOList = new ArrayList<>();
        for (Pair<Object, Object> pair : updateList) {
            if (!(pair.getLeft() instanceof  TxOutCacheKey)){
                log.error("update key is not the type of TxOutCacheKey error");
                throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_DATA_TYPE_ERROR_EXCEPTION);
            }
            txOutPOList.add((TxOutPO)pair.getRight());
        }

        return utxodbHandler.batchUpdate(txOutPOList);
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
