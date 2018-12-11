package com.higgs.trust.slave.model.convert;

import com.higgs.trust.slave.dao.po.block.BlockPO;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.StateRootHash;

/**
 * @Description:
 * @author: pengdi
 **/
public class BlockConvert {

    public static BlockHeader convertBlockPOToBlockHeader(BlockPO blockPO) {
        BlockHeader header = new BlockHeader();
        header.setBlockTime(blockPO.getBlockTime().getTime());
        header.setHeight(blockPO.getHeight());
        header.setBlockHash(blockPO.getBlockHash());
        header.setPreviousHash(blockPO.getPreviousHash());
        header.setVersion(blockPO.getVersion());
        header.setTotalTxNum(blockPO.getTotalTxNum());

        StateRootHash stateRootHash = new StateRootHash();
        stateRootHash.setTxRootHash(blockPO.getTxRootHash());
        stateRootHash.setTxReceiptRootHash(blockPO.getTxReceiptRootHash());
        stateRootHash.setAccountRootHash(blockPO.getAccountRootHash());
        stateRootHash.setContractRootHash(blockPO.getContractRootHash());
        stateRootHash.setPolicyRootHash(blockPO.getPolicyRootHash());
        stateRootHash.setRsRootHash(blockPO.getRsRootHash());
        stateRootHash.setCaRootHash(blockPO.getCaRootHash());
        stateRootHash.setStateRoot(blockPO.getStateRootHash());
        header.setStateRootHash(stateRootHash);
        return header;
    }

}
