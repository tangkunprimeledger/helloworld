package com.higgs.trust.slave.core;

import com.higgs.trust.evmcontract.core.Bloom;
import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.core.TransactionResultInfo;
import com.higgs.trust.evmcontract.crypto.HashUtil;
import com.higgs.trust.evmcontract.datasource.DbSource;
import com.higgs.trust.evmcontract.db.BlockStore;
import com.higgs.trust.evmcontract.db.TransactionStore;
import com.higgs.trust.evmcontract.facade.BlockStoreAdapter;
import com.higgs.trust.evmcontract.facade.ContractExecutionResult;
import com.higgs.trust.evmcontract.trie.Trie;
import com.higgs.trust.evmcontract.trie.TrieImpl;
import com.higgs.trust.evmcontract.util.ByteUtil;
import com.higgs.trust.evmcontract.util.RLP;
import com.higgs.trust.evmcontract.util.RLPList;
import com.higgs.trust.evmcontract.vm.LogInfo;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author duhongming
 * @date 2018/11/30
 */
@Component
public class Blockchain {

    private boolean initialized;
    private TransactionStore transactionStore;
    private BlockHeader lastBlockHeader;
    private Repository repositorySnapshot;
    private List<TransactionResultInfo> receipts;

    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private DbSource<byte[]> dbSource;
    @Autowired
    private Repository repository;

    private final BlockStore blockStore;

    public Blockchain() {
        this.transactionStore = new TransactionStore(dbSource);
        this.blockStore = createBlockStore();
    }

    public static byte[] calcReceiptsTrie(List<ContractExecutionResult> receipts) {
        Trie receiptsTrie = new TrieImpl();

        if (receipts == null || receipts.isEmpty()) {
            return HashUtil.EMPTY_TRIE_HASH;
        }

        for (int i = 0; i < receipts.size(); i++) {
            receiptsTrie.put(RLP.encodeInt(i), receipts.get(i).getReceiptTrieEncoded());
        }
        return receiptsTrie.getRootHash();
    }

    private BlockStore createBlockStore() {
        BlockStore blockStore = new BlockStoreAdapter() {
            @Override
            public byte[] getBlockHashByNumber(long blockNumber, byte[] branchBlockHash) {
                Block block = blockRepository.getBlock(blockNumber);
                if (block != null) {
                    return Hex.decode(block.getBlockHeader().getBlockHash());
                }
                return null;
            }
        };
        return blockStore;
    }

    @PostConstruct
    public void init() {
        if (initialized) {
            return;
        }
        init0();
    }

    private synchronized void init0() {
        if (initialized) {
            return;
        }
        Long maxHeight = blockRepository.getMaxHeight();
        lastBlockHeader = blockRepository.getBlockHeader(maxHeight);
        initialized = true;
    }

    public synchronized void startExecuteBlock() {
        String root = lastBlockHeader.getStateRootHash().getStateRoot();
        receipts = new ArrayList<>();
        if (StringUtils.isNotEmpty(root)) {
            repositorySnapshot = repository.getSnapshotTo(Hex.decode(lastBlockHeader.getStateRootHash().getStateRoot()));
        } else {
            repositorySnapshot = repository.getSnapshotTo(null);
        }
    }

    public synchronized void finishExecuteBlock(BlockHeader blockHeader) {
        Bloom logBloom = new Bloom();
        for (TransactionResultInfo result : receipts) {
            logBloom.or(result.getBloomFilter());
        }

        TrieImpl trie = new TrieImpl();
        for (TransactionResultInfo result : receipts) {
            transactionStore.put(result.getTxHash(), result);
            trie.put(result.getTxHash(), result.getEncoded());
        }

        long height = blockHeader.getHeight();
        MiniBlock miniBlock = new MiniBlock(height, logBloom.getData(), trie.getRootHash());
        dbSource.put(ByteUtil.longToBytes(height), miniBlock.getEncoded());
        transactionStore.flush();

        receipts = null;
        repository = repositorySnapshot;
        repositorySnapshot = null;
        lastBlockHeader = blockHeader;
    }

    public void putResultInfo(TransactionResultInfo result) {
        receipts.add(result);
    }

    public synchronized void setLastBlockHeader(BlockHeader blockHeader) {
        lastBlockHeader = blockHeader;
        repository = repository.getSnapshotTo(Hex.decode(blockHeader.getStateRootHash().getStateRoot()));
    }

    public BlockHeader getLastBlockHeader() {
        return lastBlockHeader;
    }

    public Repository getRepository() {
        return repository;
    }

    public Repository getRepositorySnapshot() {
        return repositorySnapshot;
    }

    public Repository getRepositorySnapshot(byte[] root) {
        return repository.getSnapshotTo(root);
    }

    public BlockStore getBlockStore() {
        return blockStore;
    }


    private class MiniBlock {
        private long height;
        private byte[] logsBloom;
        private byte[] receiptsRoot;

        private byte[] rlpEncoded;

        public MiniBlock(long height, byte[] logsBloom, byte[] receiptsRoot) {
            this.height = height;
            this.logsBloom = logsBloom;
            this.receiptsRoot = receiptsRoot;
        }

        public MiniBlock(byte[] rlp) {
            this.rlpEncoded = rlp;
            RLPList rlpList = RLP.decode2(rlp);

            BigInteger integer = RLP.decodeBigInteger(rlpList.get(0).getRLPData(), 0);
            this.height = integer.longValue();
            this.logsBloom = rlpList.get(1).getRLPData();
            this.receiptsRoot = rlpList.get(2).getRLPData();
        }

        public byte[] getEncoded() {
            if (rlpEncoded != null) {
                return rlpEncoded;
            }

            rlpEncoded = RLP.encodeList(
                    RLP.encodeBigInteger(BigInteger.valueOf(height)),
                    RLP.encodeElement(logsBloom),
                    RLP.encodeElement(receiptsRoot)
            );

            return rlpEncoded;
        }

        public long getHeight() {
            return height;
        }

        public byte[] getLogsBloom() {
            return logsBloom;
        }

        public byte[] getReceiptsRoot() {
            return receiptsRoot;
        }
    }

    public static void main(String[] args) {
        System.out.println("kdkd");
    }
}
