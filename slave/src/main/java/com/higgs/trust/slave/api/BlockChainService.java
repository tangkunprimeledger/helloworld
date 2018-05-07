package com.higgs.trust.slave.api;

import com.higgs.trust.slave.api.vo.RespData;
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
}
