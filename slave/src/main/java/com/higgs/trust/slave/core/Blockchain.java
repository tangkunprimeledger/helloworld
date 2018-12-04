package com.higgs.trust.slave.core;

import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.db.BlockStore;
import com.higgs.trust.evmcontract.facade.BlockStoreAdapter;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.model.bo.BlockHeader;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author duhongming
 * @date 2018/11/30
 */
@Component
public class Blockchain {

    private BlockHeader lastBlockHeader;

    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private Repository repository;

    private final BlockStore blockStore;

    public Blockchain() {
        this.blockStore = createBlockStore();
    }

    private BlockStore createBlockStore() {
        BlockStore blockStore = new BlockStoreAdapter() {
            @Override
            public byte[] getBlockHashByNumber(long blockNumber, byte[] branchBlockHash) {
                return Hex.decode(lastBlockHeader.getBlockHash());
            }
        };
        return blockStore;
    }

    public void init() {
        Long maxHeight = blockRepository.getMaxHeight();
        lastBlockHeader = blockRepository.getBlockHeader(maxHeight);
    }

    public void startExecuteBlock() {

    }

    public void finishExecuteBlock() {

    }

    public void setLastBlockHeader(BlockHeader blockHeader) {
        lastBlockHeader = blockHeader;
    }

    public BlockHeader getLastBlockHeader() {
        return lastBlockHeader;
    }

    public Repository getRepository() {
        return repository;
    }

//    public Repository getRepository(byte[] stateRoot) {
//        return repository.getSnapshotTo(stateRoot);
//    }

    public BlockStore getBlockStore() {
        return blockStore;
    }
}
