package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.api.vo.UTXOVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import com.higgs.trust.slave.dao.utxo.TxOutDao;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * txOut repository
 *
 * @author lingchao
 * @create 2018年04月12日21:35
 */
@Repository
@Slf4j
public class TxOutRepository {

    @Autowired
    private TxOutDao txOutDao;

    /**
     * query txOut by txId, index and actionIndex
     *
     * @param txId
     * @param index
     * @param actionIndex
     * @return
     */
    public TxOutPO queryTxOut(String txId, Integer index, Integer actionIndex) {
        return txOutDao.queryTxOut(txId, index, actionIndex);
    }

    /**
     * batch insert
     *
     * @param txOutPOList
     * @return
     */
    //TODO lingchao 检查是否会存在部分数据入库成功
    public boolean batchInsert(List<TxOutPO> txOutPOList) {
        int affectRows = 0;
        try {
            affectRows = txOutDao.batchInsert(txOutPOList);
        } catch (DuplicateKeyException e) {
            log.error("batch insert UTXO fail, because there is DuplicateKeyException for txOutPOList:", txOutPOList);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        return affectRows == txOutPOList.size();
    }

    /**
     * batch update
     *
     * @param txOutPOList
     * @return
     */
    public boolean batchUpdate(List<TxOutPO> txOutPOList) {
        return txOutPOList.size() == txOutDao.batchUpdate(txOutPOList);
    }

    public List<UTXOVO> queryTxOutByTxId(String txId) {
        List<TxOutPO> list = txOutDao.queryByTxId(txId);
        int i = 0;
        do {
            for (TxOutPO po : list) {
                UTXOVO vo = new UTXOVO();
//                vo.setPreUTXOVO(txOutDao.queryByTxId(txId));
            }
            i++;
        } while (i < 5);
        return null;
    }

    public List<TxOutPO> queryTxOutsByTxId (String txId, int i) {
        if (i < 5) {
            List<TxOutPO> list = txOutDao.queryByTxId(txId);
        } else {

        }
        return null;
    }
}
