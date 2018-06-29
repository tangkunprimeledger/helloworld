package com.higgs.trust.rs.core.service;

import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.common.utils.CoreTransactionConvertor;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.slave.api.AccountInfoService;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/05/13 15:58
 * @desc block chain service
 */
@Slf4j
@Service
public class RsChainServiceImpl implements RsBlockChainService {
    public static final String CHAIN_OWNER_KEY = "CHAIN_OWNER";
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
    public List<UTXOVO> queryUTXO(String txId) {

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
    @Override
    public boolean isExistedCurrency(String currency) {
        return blockChainService.isExistedCurrency(currency);
    }

    /**
     * query System Property by key
     *
     * @param key
     * @return
     */
    @Override
    public SystemPropertyVO querySystemPropertyByKey(String key) {
        return blockChainService.querySystemPropertyByKey(key);
    }

    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    @Override
    public List<UTXO> queryUTXOList(List<TxIn> inputList) {
        return blockChainService.queryUTXOList(inputList);
    }

    /**
     * get utxo action type
     *
     * @param name
     * @return
     */
    @Override
    public UTXOActionTypeEnum getUTXOActionType(String name) {
        return blockChainService.getUTXOActionType(name);
    }

    /**
     * query chain owner
     *
     * @return
     */
    @Override
    public String queryChainOwner() {
        SystemPropertyVO systemPropertyVO = blockChainService.querySystemPropertyByKey(CHAIN_OWNER_KEY);
        if (null == systemPropertyVO) {
            log.error("there is no chain_owner in this node for key {}, please check system property DB", CHAIN_OWNER_KEY);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_GET_CHAIN_OWNER_NULL_ERROR);
        }
        return systemPropertyVO.getValue();
    }
}
