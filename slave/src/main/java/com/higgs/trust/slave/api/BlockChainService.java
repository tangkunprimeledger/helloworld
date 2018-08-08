package com.higgs.trust.slave.api;

import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;

import java.util.List;

/**
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
    RespData<List<TransactionVO>> submitTransactions(List<SignedTransaction> transactions);

    RespData submitTransaction(SignedTransaction transaction);

    List<BlockHeader> listBlockHeaders(long startHeight, int size);

    List<Block> listBlocks(long startHeight, int size);

    /**
     * query block
     *
     * @param req
     * @return
     */
    @Deprecated
    PageVO<BlockVO> queryBlocks(QueryBlockVO req);

    /**
     * query transaction
     *
     * @param req
     * @return
     */
    @Deprecated
    PageVO<CoreTransactionVO> queryTransactions(QueryTransactionVO req);

    /**
     * query utxo by transaction id
     *
     * @param txId
     * @return
     */
    List<UTXOVO> queryUTXOByTxId(String txId);

    /**
     * check whether the identity is existed
     *
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
     *
     * @param key
     * @return
     */
    SystemPropertyVO querySystemPropertyByKey(String key);

    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    List<UTXO> queryUTXOList(List<TxIn> inputList);

    /**
     * get utxo action type
     *
     * @param name
     * @return
     */
    UTXOActionTypeEnum getUTXOActionType(String name);

    /**
     * query header by height
     *
     * @param blockHeight
     * @return
     */
    BlockHeader getBlockHeader(Long blockHeight);

    /**
     * query max
     *
     * @return
     */
    BlockHeader getMaxBlockHeader();

    /**
     * query max height for block
     *
     * @return
     */
    Long getMaxBlockHeight();
    /**
     * query block by condition and page
     *
     * @param req
     * @return
     */
    List<BlockVO> queryBlocksByPage(QueryBlockVO req);

    /**
     * query transaction by condition and page
     *
     * @param req
     * @return
     */
    List<CoreTransactionVO> queryTxsByPage(QueryTransactionVO req);

    /**
     * query block info by height
     * @param height
     * @return
     */
    BlockVO queryBlockByHeight(Long height);

    /**
     *  query tx info by tx_id
     * @param txId
     * @return
     */
    CoreTransactionVO queryTxById(String txId);

    /**
     * query by ids
     *
     * @param txIds
     * @return
     */
    List<CoreTransactionVO> queryTxByIds(List<String> txIds);
}
