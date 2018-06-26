package com.higgs.trust.rs.core.service;

import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.slave.api.AccountInfoService;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/05/13 15:58
 * @desc block chain service
 */
@Service
public class RsChainServiceImpl implements RsBlockChainService {

    @Autowired
    private BlockChainService blockChainService;

    @Autowired
    private AccountInfoService accountInfoService;

    @Override
    public PageVO<BlockVO> queryBlock(QueryBlockVO req) {
        return blockChainService.queryBlocks(req);
    }

    @Override
    public PageVO<CoreTransactionVO> queryTransaction(QueryTransactionVO req) {
        return blockChainService.queryTransactions(req);
    }

    @Override
    public PageVO<AccountInfoVO> queryAccount(QueryAccountVO req) {
        return accountInfoService.queryAccountInfo(req);
    }

    @Override
    public List<UTXOVO> queryUtxo(String txId) {

        return blockChainService.queryUTXOByTxId(txId);
    }

    /**
     * check whether the identity is existed
     *
     * @param identity
     * @return
     */
    @Override
    public boolean isExistedIdentity(String identity) {
        return blockChainService.isExistedIdentity(identity);
    }
    /**
     * check currency
     *
     * @param currency
     * @return
     */
    @Override public boolean isExistedCurrency(String currency) {
        return blockChainService.isExistedCurrency(currency);
    }
}
