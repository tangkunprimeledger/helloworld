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
    PageVO<BlockVO> queryBlock(QueryBlockVO req);

    /**
     * query transaction
     *
     * @param req
     * @return
     */
    PageVO<CoreTransactionVO> queryTransaction(QueryTransactionVO req);

    /**
     * query account
     *
     * @param req
     * @return
     */
    PageVO<AccountInfoVO> queryAccount(QueryAccountVO req);

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
     * process UTXO contract
     *
     * @param coreTransaction
     * @return
     */
    boolean processContract(CoreTransaction coreTransaction);
}
