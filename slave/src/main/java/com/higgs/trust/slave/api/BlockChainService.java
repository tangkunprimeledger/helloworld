package com.higgs.trust.slave.api;

import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;

import java.util.List;

/**
 *
 * @author pengdi
 * @date 
 */
public interface BlockChainService {

    /**
     * create transactions
     *
     * @param transactions
     * @return
     */
    RespData submitTransactions(List<SignedTransaction> transactions);

    RespData submitTransaction(SignedTransaction transaction);

    List<BlockHeader> listBlockHeaders(long startHeight, int size);

    List<Block> listBlocks(long startHeight, int size);

    /**
     * query block
     * @param req
     * @return
     */
    List<BlockVO> queryBlocks(QueryBlockVO req);

    /**
     * query transaction
     * @param req
     * @return
     */
    List<CoreTransactionVO> queryTransactions(QueryTxVO req);


    List<UTXOVO> queryUTXOByTxId(String txId);

    /**
     * check whether the identity is existed
     * @param identity
     * @return
     */
    boolean isExistedIdentity(String identity);
}
