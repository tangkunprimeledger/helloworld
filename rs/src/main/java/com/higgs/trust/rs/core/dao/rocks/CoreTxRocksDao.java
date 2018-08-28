package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class CoreTxRocksDao extends RocksBaseDao<String, CoreTransactionPO>{
    @Override protected String getColumnFamilyName() {
        return "coreTransaction";
    }

    public void save(CoreTransactionPO po) {

    }

    /**
     * db transaction
     * @param po
     */
    public void saveWithTransaction(CoreTransactionPO po) {
    }

    public List<CoreTransactionPO> queryByTxIds(List<String> txIdList) {
        return null;
    }

    public void updateSignDatas(String txId, String signJSON) {
    }

    public void saveExecuteResultAndHeight(String txId, String code, String respCode, String respMsg, Long blockHeight) {
    }

    public void batchInsert(List<CoreTransactionPO> coreTransactionPOList) {
    }

    public void batchUpdate(List<RsCoreTxVO> txs, Long blockHeight) {
    }
}
