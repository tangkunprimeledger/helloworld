package com.higgs.trust.slave.api;

import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.config.SystemProperty;

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
    PageVO<BlockVO> queryBlocks(QueryBlockVO req);

    /**
     * query transaction
     * @param req
     * @return
     */
    PageVO<CoreTransactionVO> queryTransactions(QueryTransactionVO req);

    /**
     * query utxo by transaction id
     * @param txId
     * @return
     */
    List<UTXOVO> queryUTXOByTxId(String txId);

    /**
     * check whether the identity is existed
     * @param identity
     * @return
     */
    boolean isExistedIdentity(String identity);

    /**
     * check currency
     *
     * @param currency
     * @return
     */
    boolean isExistedCurrency(String currency);

    /**
     * query System Property by key
     * @param key
     * @return
     */
    SystemPropertyVO querySystemPropertyByKey(String key);

}
