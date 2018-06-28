package com.higgs.trust.rs.custom.api.blockchain;

import com.higgs.trust.rs.custom.api.vo.blockchain.BlockQueryVO;
import com.higgs.trust.rs.custom.api.vo.blockchain.TxQueryVO;
import com.higgs.trust.rs.custom.model.RespData;

/**
 * @author tangfashuang
 */
public interface BlockService {
    /**
     * query block
     * @param req
     * @return
     */
    RespData queryBlock(BlockQueryVO req);

    /**
     * query transaction
     * @param req
     * @return
     */
    RespData queryTx(TxQueryVO req);

    /**
     * query uxto
     * @param txId
     * @return
     */
    RespData queryUTXO(String txId);
}
