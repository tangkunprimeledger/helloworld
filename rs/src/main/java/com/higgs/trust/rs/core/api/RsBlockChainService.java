package com.higgs.trust.rs.core.api;

import com.higgs.trust.slave.api.vo.*;

import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/05/13 15:51
 * @desc
 */
public interface RsBlockChainService {

    /**
     * query block
     * @param req
     * @return
     */
    PageVO<BlockVO> queryBlock(QueryBlockVO req);

    /**
     * query transaction
     * @param req
     * @return
     */
    PageVO<CoreTransactionVO> queryTransaction(QueryTransactionVO req);

    /**
     * query account
     * @param req
     * @return
     */
    PageVO<AccountInfoVO> queryAccount(QueryAccountVO req);

    /**
     * query utxo
     * @param txId
     * @return
     */
    List<UTXOVO> queryUtxo(String txId);

    /**
     * check whether the identity is existed
     * @param identity
     * @return
     */
    boolean isExistedIdentity(String identity);

    /**
     * check curency
     * @param currency
     * @return
     */
    boolean isExistedCurrency(String currency);
}
