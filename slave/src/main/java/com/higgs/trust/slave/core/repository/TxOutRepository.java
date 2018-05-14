package com.higgs.trust.slave.core.repository;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.vo.UTXOVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import com.higgs.trust.slave.dao.utxo.TxOutDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${trust.utxo.display:2}")
    private int DISPLAY;

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
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        List<UTXOVO> utxovoList = BeanConvertor.convertList(list, UTXOVO.class);

        for (UTXOVO vo : utxovoList) {
            queryTxOutBySTxId(vo, txId, DISPLAY);
        }
        return utxovoList;
    }

    private void queryTxOutBySTxId(UTXOVO vo, String txId, int i) {

        List<TxOutPO> list = txOutDao.queryBySTxId(txId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        List<UTXOVO> utxovoList = BeanConvertor.convertList(list, UTXOVO.class);
        vo.setPreUTXOVO(utxovoList);

        if (i == 0) {
            return;
        } else {
            i -= 1;
            for (UTXOVO utxovo : utxovoList) {
                queryTxOutBySTxId(utxovo, utxovo.getTxId(), i);
            }
        }
    }
}
