package com.higgs.trust.rs.core.service;

import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.rs.core.contract.RsUTXOSmartContract;
import com.higgs.trust.slave.api.AccountInfoService;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.vo.*;
import com.higgs.trust.slave.core.service.contract.UTXOExecuteContextData;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/05/13 15:58
 * @desc block chain service
 */
@Slf4j @Service public class RsChainServiceImpl implements RsBlockChainService {
    public static final String CHAIN_OWNER_KEY = "CHAIN_OWNER";
    @Autowired private BlockChainService blockChainService;

    @Autowired private AccountInfoService accountInfoService;

    @Autowired private RsUTXOSmartContract rsUTXOSmartContract;

    @Override public PageVO<BlockVO> queryBlock(QueryBlockVO req) {
        return blockChainService.queryBlocks(req);
    }

    @Override public PageVO<CoreTransactionVO> queryTransaction(QueryTransactionVO req) {
        return blockChainService.queryTransactions(req);
    }

    @Override public PageVO<AccountInfoVO> queryAccount(QueryAccountVO req) {
        return accountInfoService.queryAccountInfo(req);
    }

    @Override public List<UTXOVO> queryUTXO(String txId) {

        return blockChainService.queryUTXOByTxId(txId);
    }

    /**
     * check whether the identity is existed
     *
     * @param identity
     * @return
     */
    @Override public boolean isExistedIdentity(String identity) {
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

    /**
     * query System Property by key
     *
     * @param key
     * @return
     */
    @Override public SystemPropertyVO querySystemPropertyByKey(String key) {
        return blockChainService.querySystemPropertyByKey(key);
    }

    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    @Override public List<UTXO> queryUTXOList(List<TxIn> inputList) {
        return blockChainService.queryUTXOList(inputList);
    }

    /**
     * get utxo action type
     *
     * @param name
     * @return
     */
    @Override public UTXOActionTypeEnum getUTXOActionType(String name) {
        return blockChainService.getUTXOActionType(name);
    }

    /**
     * query chain owner
     *
     * @return
     */
    @Override public String queryChainOwner() {
        SystemPropertyVO systemPropertyVO = blockChainService.querySystemPropertyByKey(CHAIN_OWNER_KEY);
        if (null == systemPropertyVO) {
            log.error("there is no chain_owner in this node for key {}, please check system property DB",
                CHAIN_OWNER_KEY);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_GET_CHAIN_OWNER_NULL_ERROR);
        }
        return systemPropertyVO.getValue();
    }

    /**
     * query by height
     *
     * @param blockHeight
     * @return
     */
    @Override public BlockHeader getBlockHeader(Long blockHeight) {
        return blockChainService.getBlockHeader(blockHeight);
    }

    @Override public BlockHeader getMaxBlockHeader() {
        return blockChainService.getMaxBlockHeader();
    }

    @Override public Long getMaxBlockHeight() {
        return blockChainService.getMaxBlockHeight();
    }

    /**
     * process UTXO contract
     *
     * @param coreTransaction
     * @return
     */
    @Override public boolean processContract(CoreTransaction coreTransaction) {
        //check arguments
        if (null == coreTransaction) {
            log.error("process for contract arguments error, coreTransaction is null");
            throw new IllegalArgumentException("process for contract arguments error, coreTransaction is null");
        }
        return processActions(coreTransaction.getActionList());

    }

    @Override public List<BlockVO> queryBlocksByPage(QueryBlockVO req) {
        return blockChainService.queryBlocksByPage(req);
    }

    @Override public List<CoreTransactionVO> queryTxsByPage(QueryTransactionVO req) {
        return blockChainService.queryTxsByPage(req);
    }

    @Override public BlockVO queryBlockByHeight(Long height) {
        return blockChainService.queryBlockByHeight(height);
    }

    @Override public CoreTransactionVO queryTxById(String txId) {
        return blockChainService.queryTxById(txId);
    }

    @Override public List<CoreTransactionVO> queryTxByIds(List<String> txIds) {
        return blockChainService.queryTxByIds(txIds);
    }

    /**
     * process contract
     *
     * @param actionList
     * @return
     */
    private boolean processActions(List<Action> actionList) {
        if (CollectionUtils.isEmpty(actionList)) {
            log.error("There is no actionList");
            return false;
        }

        //when the action is UTXO we execute contract, otherwise not .
        //if there is no UTXO action return true.
        int UTXOActionNum = 0;
        for (Action action : actionList) {
            if (action.getType() != ActionTypeEnum.UTXO) {
                continue;
            }
            //execute contract
            UTXOActionNum = UTXOActionNum + 1;
            UTXOAction utxoAction = (UTXOAction)action;
            ExecuteContextData data = new UTXOExecuteContextData().setAction(utxoAction);
            if (!rsUTXOSmartContract.execute(utxoAction.getContractAddress(), data)) {
                log.info("UTXO contract process result is not pass");
                return false;
            }
        }
        //check the num of UTXO Action in coreTransaction
        if (0 == UTXOActionNum) {
            log.error("There is no UTXO Action in actionList :{}", actionList);
            return false;
        }
        return true;
    }
}
