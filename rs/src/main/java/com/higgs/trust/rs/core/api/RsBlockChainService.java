package com.higgs.trust.rs.core.api;

import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;

import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/05/13 15:51
 * @desc
 */
public interface RsBlockChainService {

    /**
     * query block
     *
     * @param req
     * @return
     */
    @Deprecated PageVO<BlockVO> queryBlock(QueryBlockVO req);

    /**
     * query transaction
     *
     * @param req
     * @return
     */
    @Deprecated PageVO<CoreTransactionVO> queryTransaction(QueryTransactionVO req);

    /**
     * query account
     *
     * @param req
     * @return
     */
    @Deprecated PageVO<AccountInfoVO> queryAccount(QueryAccountVO req);

    /**
     *query accounts
     * @param req
     * @return
     */
     List<AccountInfoVO> queryAccountsByPage(QueryAccountVO req);
    /**
     * query utxo
     *
     * @param txId
     * @return
     */
    List<UTXOVO> queryUTXO(String txId);

    /**
     * check whether the identity is existed
     *
     * @param identity
     * @return
     */
    boolean isExistedIdentity(String identity);

    /**
     * check curency
     *
     * @param currency
     * @return
     */
    boolean isExistedCurrency(String currency);

    /**
     * check whether the contract address is existed
     *
     * @param address
     * @return
     */
    boolean isExistedContractAddress(String address);

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
     * query chain_owner
     *
     * @return
     */
    String queryChainOwner();

    /**
     * query by height
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
     * process UTXO contract
     *
     * @param coreTransaction
     * @return
     */
    boolean processContract(CoreTransaction coreTransaction);

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
     *
     * @param height
     * @return
     */
    BlockVO queryBlockByHeight(Long height);

    /**
     * query tx info by tx_id
     *
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

    /**
     *
     * @return
     */
    List<NodeInfoVO> queryPeersInfo();
}
