package com.higgs.trust.evmcontract.facade;

import com.higgs.trust.evmcontract.core.Block;
import com.higgs.trust.evmcontract.db.BlockStore;

import java.math.BigInteger;
import java.util.List;

/**
 * @author Chen Jiawei
 * @date 2018-11-21
 */
public class BlockStoreAdapter implements BlockStore {
    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {
        return new byte[0];
    }

    @Override
    public byte[] getBlockHashByNumber(long blockNumber, byte[] branchBlockHash) {
        return new byte[0];
    }

    @Override
    public Block getChainBlockByNumber(long blockNumber) {
        return null;
    }

    @Override
    public Block getBlockByHash(byte[] hash) {
        return null;
    }

    @Override
    public boolean isBlockExist(byte[] hash) {
        return false;
    }

    @Override
    public List<byte[]> getListHashesEndWith(byte[] hash, long qty) {
        return null;
    }

    @Override
    public List<Block> getListBlocksEndWith(byte[] hash, long qty) {
        return null;
    }

    @Override
    public void saveBlock(Block block, BigInteger totalDifficulty, boolean mainChain) {

    }

    @Override
    public BigInteger getTotalDifficultyForHash(byte[] hash) {
        return null;
    }

    @Override
    public BigInteger getTotalDifficulty() {
        return null;
    }

    @Override
    public Block getBestBlock() {
        return null;
    }

    @Override
    public long getMaxNumber() {
        return 0;
    }

    @Override
    public void flush() {

    }

    @Override
    public void reBranch(Block forkBlock) {

    }

    @Override
    public void load() {

    }

    @Override
    public void close() {

    }
}
